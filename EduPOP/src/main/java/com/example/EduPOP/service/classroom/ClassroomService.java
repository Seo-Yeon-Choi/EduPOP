package com.example.EduPOP.service.classroom;

import com.example.EduPOP.controller.classroom.dto.ClassroomCreateRequest;
import com.example.EduPOP.controller.classroom.dto.ClassroomListResponse;
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
    public void updateStatus(Long classId, String statusName) {
        if (classId == null || statusName == null) {
            throw new IllegalArgumentException("필수 파라미터가 누락되었습니다.");
        }

        // 잘못된 문자열이 들어오면 여기서 걸러냄 (안전장치)
        Classroom.ClassStatus status = Classroom.ClassStatus.valueOf(statusName.trim().toUpperCase());

        // DB에는 순수 대문자 문자열("CLOSED")로 전달
        classroomMapper.updateStatus(classId, status.name());
    }

    /**
     * 다중 반 상태 일괄 변경
     */
    @Transactional
    public void updateStatusesBulk(List<Long> classIds, String statusName) {
        // 필수 파라미터 누락 검증
        if (classIds == null || classIds.isEmpty() || statusName == null || statusName.trim().isEmpty()) {
            throw new IllegalArgumentException("선택된 반이 없거나 상태값이 올바르지 않습니다.");
        }

        // 도메인 Enum 유효성 검증 (오타 방지 및 대문자 정규화)
        Classroom.ClassStatus classStatus;
        try {
            classStatus = Classroom.ClassStatus.valueOf(statusName.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("지원하지 않는 반 상태값입니다: " + statusName);
        }

        // 검증된 Enum의 정확한 문자열 이름("ACTIVE", "CLOSED")으로 Mapper 호출
        classroomMapper.updateStatusesBulk(classIds, classStatus.name());
    }

}
