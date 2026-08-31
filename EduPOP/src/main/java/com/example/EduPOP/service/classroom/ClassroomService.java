package com.example.EduPOP.service.classroom;

import com.example.EduPOP.controller.classroom.dto.ClassroomCreateRequest;
import com.example.EduPOP.controller.classroom.dto.ClassroomDetailResponse;
import com.example.EduPOP.controller.classroom.dto.ClassroomListResponse;
import com.example.EduPOP.controller.classroom.dto.ClassroomUpdateRequest;
import com.example.EduPOP.domain.classroom.ClassTeacher;
import com.example.EduPOP.domain.classroom.Classroom;
import com.example.EduPOP.repository.classroom.ClassroomMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassroomService {
    private final ClassroomMapper classroomMapper;

    /**
     * 반 생성 및 다중 강사 배정 (DTO 도입 및 벌크 인서트 리팩토링)
     * - 학원 내 반 이름 중복 검사
     * - classes 테이블 INSERT 및 자동 생성 PK 획득
     * - class_teachers 테이블에 배정 강사진 벌크 INSERT (I/O 최적화)
     */
    @Transactional
    public Long createClass(ClassroomCreateRequest request){
        // 동일 학원 내 중복 반 명칭 사전 검증
        Classroom existingClass = classroomMapper
                .findByNameAndAcademyId(request.getName(), request.getAcademyId());
        if(existingClass != null){
            throw new IllegalArgumentException("이미 동일한 이름의 반이 존재합니다.");
        }

        // DTO -> Domain Entity 변환
        Classroom classroom = new Classroom();
        classroom.setAcademyId(request.getAcademyId());
        classroom.setName(request.getName());
        classroom.setTargetGrade(request.getTargetGrade());
        classroom.setMaxStudents(request.getMaxStudents());
        classroom.setDescription(request.getDescription());

        // 반 기본 정보 저장 (useGeneratedKeys로 classId 자동 세팅)
        classroomMapper.insertClass(classroom);

        // 배정된 강사진 매핑 데이터 벌크 인서트 변환 및 저장 (N:M 해소)
        if(request.getTeachers() != null && !request.getTeachers().isEmpty()){
            List<ClassTeacher> teachersToInsert = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();

            for (ClassroomCreateRequest.TeacherRequest teacherDto : request.getTeachers()){
                ClassTeacher teacher = new ClassTeacher();
                teacher.setClassId(classroom.getClassId()); // 생성된 반 PK 주입
                teacher.setTeacherId(teacherDto.getTeacherId());
                teacher.setRoleType(teacherDto.getRoleType());
                teacher.setCreatedAt(now); // 서버 단 시간 명시적 세팅
                teachersToInsert.add(teacher);
            }
            // 한 번의 쿼리로 다중 강사 배정 정보 일괄 등록 (데이터베이스 네트워크 Round-trip 감소)
            classroomMapper.insertClassTeachers(teachersToInsert);
        }
        return classroom.getClassId();
    }

    @Transactional(readOnly = true)
    public List<ClassroomListResponse> findAllByAcademyId(Long academyId, String status){
        // 방어 로직: 학원 번호가 전달되지 않았을 경우 빈 리스트 반환 또는 예외 방어
        if (academyId == null) {
            return Collections.emptyList();
        }

        // Mapper를 통해 DB(MyBatis)에서 조인된 반 목록 데이터를 가져와 그대로 반환
        return classroomMapper.findAllByAcademyId(academyId, status);
    }

    /**
     * 단건 반 상태 변경
     */
    @Transactional
    public void updateStatus(Long classId, Classroom.ClassStatus status) {
        // 매퍼 호출 (매퍼에는 classId와 status.name() 전달)
        classroomMapper.updateStatus(classId, status.name());
    }

    /**
     * 다중 반 상태 일괄 변경
     */
    @Transactional
    public void updateStatusesBulk(List<Long> classIds, Classroom.ClassStatus status) {
        if (classIds == null || classIds.isEmpty()) {
            return;
        }
        classroomMapper.updateStatusesBulk(classIds, status.name());
    }

    /**
     * 특정 반 상세 정보
     */
    public ClassroomDetailResponse findById(Long classId) {
        ClassroomDetailResponse detail = classroomMapper.findById(classId);
        if (detail == null) {
            throw new IllegalArgumentException("반 정보를 찾을 수 없습니다.");
        }
        // 강사 목록 조립
        detail.setTeachers(classroomMapper.findTeachersByClassId(classId));
        // 💡 학생 목록 조립
        detail.setStudents(classroomMapper.findStudentsByClassId(classId));
        return detail;
    }

    /**
     *  반 기본 정보 수정
     */
    @Transactional
    public void updateClass(Long classId, ClassroomUpdateRequest request) {
        // 수정할 반이 실제 존재하는지 먼저 확인
        ClassroomDetailResponse existingClass = classroomMapper.findById(classId);
        if (existingClass == null) {
            throw new IllegalArgumentException("수정 대상 반이 존재하지 않습니다. ID: " + classId);
        }

        // DB 업데이트 수행
        classroomMapper.updateClass(
                classId,
                request.getName(),
                request.getTargetGrade(),
                request.getMaxStudents(),
                request.getStatus().name(),
                request.getDescription()
        );
    }

    /**
     *  강사 신규 추가 배정 (중복 방지 검증 포함)
     */
    @Transactional
    public void addTeacher(Long classId, Long teacherId, Classroom.TeacherRoleType roleType) {
        // 1) 이미 해당 반에 배정된 강사인지 중복 확인
        int count = classroomMapper.countClassTeacher(classId, teacherId);
        if (count > 0) {
            throw new IllegalArgumentException("이미 해당 반에 배정되어 있는 강사입니다.");
        }

        // 2) 매핑 객체 생성 및 INSERT
        ClassTeacher classTeacher = new ClassTeacher();
        classTeacher.setClassId(classId);
        classTeacher.setTeacherId(teacherId);
        classTeacher.setRoleType(roleType != null ? roleType : Classroom.TeacherRoleType.SUB);

        classroomMapper.insertClassTeacher(classTeacher);
    }

    /**
     *  강사 배정 해제
     */
    @Transactional
    public void removeTeacher(Long classId, Long teacherId) {
        classroomMapper.deleteClassTeacher(classId, teacherId);
    }

    /**
     *  학원에 소속된 강사 전체 목록 조회 (반 배정용 드롭다운 풀)
     */
    public List<ClassroomDetailResponse.TeacherInfo> findTeachersByAcademyId(Long academyId) {
        return classroomMapper.findTeachersByAcademyId(academyId);
    }

    /**
     *  수강생 신규 등록 (정원 초과 및 중복 검증)
     */
    @Transactional
    public void addStudent(Long classId, Long studentId) {
        ClassroomDetailResponse classroom = classroomMapper.findById(classId);
        if (classroom == null) {
            throw new IllegalArgumentException("존재하지 않는 반입니다.");
        }

        // 1) 정원 초과 검증
        int currentStudentCount = classroomMapper.countStudentsByClassId(classId);
        if (currentStudentCount >= classroom.getMaxStudents()) {
            throw new IllegalStateException("수강 정원(" + classroom.getMaxStudents() + "명)을 초과하여 등록할 수 없습니다.");
        }

        // 2) 중복 등록 검증
        if (classroomMapper.existsClassStudent(classId, studentId) > 0) {
            throw new IllegalArgumentException("이미 해당 반에 등록되어 있는 학생입니다.");
        }

        // 3) 학생 배정
        classroomMapper.insertClassStudent(classId, studentId);
    }

    /**
     *  수강생 퇴원 / 반 배정 제외
     */
    @Transactional
    public void removeStudent(Long classId, Long studentId) {
        classroomMapper.deleteClassStudent(classId, studentId);
    }

    // 학생 풀 조회 시 classId도 함께 전달
    public List<ClassroomDetailResponse.StudentInfo> findStudentPool(Long academyId, Long classId) {
        return classroomMapper.findStudentPoolByAcademyId(academyId, classId);
    }

    //  수강생 일괄 동기화 (전반 시 이전 반 소속 말끔히 삭제)
    @Transactional
    public void syncStudents(Long classId, List<Long> studentIds) {
        // 1. 반 정보 조회 후 정원 체크 (프론트 조작 방어)
        ClassroomDetailResponse classroom = classroomMapper.findById(classId);
        if (studentIds != null && classroom.getMaxStudents() != null && studentIds.size() > classroom.getMaxStudents()) {
            throw new IllegalArgumentException("수강 정원(" + classroom.getMaxStudents() + "명)을 초과할 수 없습니다.");
        }

        // 2. 현재 반 비우기
        classroomMapper.deleteAllStudentsByClassId(classId);

        // 3. 타 반 소속 제거 및 재배정
        if (studentIds != null && !studentIds.isEmpty()) {
            classroomMapper.deleteOtherClassMapping(studentIds);
            classroomMapper.batchInsertStudents(classId, studentIds);
        }
    }

    /**
     * 특정 반(classId)에 소속된 학생 목록 조회 (선생님 학생 관리 페이지용)
     */
    @Transactional(readOnly = true)
    public List<ClassroomDetailResponse.StudentInfo> findStudentsByClassId(Long classId) {
        if (classId == null) {
            return Collections.emptyList();
        }
        return classroomMapper.findStudentsByClassId(classId);
    }

    /**
     * 학생 기본 정보 수정 (선생님 권한: 이름, 이메일, 연락처)
     * - (※ 학생 삭제는 운영진 권한이므로 제외)
     */
    @Transactional
    public void updateStudentInfo(Long studentId, String name, String email, String phone) {
        // 실제 존재하는 사용자(학생)인지 검증 로직 추가 가능
        classroomMapper.updateStudentInfo(studentId, name, email, phone);
    }

}



