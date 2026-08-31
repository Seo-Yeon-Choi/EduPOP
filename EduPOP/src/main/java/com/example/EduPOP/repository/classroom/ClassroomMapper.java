package com.example.EduPOP.repository.classroom;

import com.example.EduPOP.controller.classroom.dto.ClassroomDetailResponse;
import com.example.EduPOP.controller.classroom.dto.ClassroomListResponse;
import com.example.EduPOP.domain.classroom.ClassTeacher;
import com.example.EduPOP.domain.classroom.Classroom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 반 관리 및 배정 매퍼 인터페이스 (MyBatis)
 * - classes 및 class_teachers 테이블 대상 데이터 접근 계층
 */
@Mapper
public interface ClassroomMapper {

    // 반 등록 (PK 자동 생성)
    int insertClass(Classroom classroom);

    // 반-강사 배정 매핑 등록 (단건)
    int insertClassTeacher(ClassTeacher classTeacher);

    // 반-강사 다중 배정 매핑 벌크 등록 (벌크 인서트 최적화)
    int insertClassTeachers(@Param("teachers") List<ClassTeacher> teachers);

    // 같은 학원 내 반 이름 중복 조회
    Classroom findByNameAndAcademyId(@Param("name") String name,
                                     @Param("academyId") Long academyId);

    // 특정 학원의 반 목록 전체 조회
    List<ClassroomListResponse> findAllByAcademyId(@Param("academyId") Long academyId, @Param("status") String status);

    // 특정 반 조회
    Classroom findByClassId(@Param("classId") Long classId);

    // 반 단건 상태 변경
    void updateStatus(@Param("classId") Long classId, @Param("status") String status);

    // 반 다중 일괄 상태 변경
    void updateStatusesBulk(@Param("classIds") List<Long> classIds, @Param("status") String status);

    // 반 정보 상세 페이지
    ClassroomDetailResponse findById(@Param("classId") Long classId);
    List<ClassroomDetailResponse.TeacherInfo> findTeachersByClassId(@Param("classId") Long classId);

    void updateClass(@Param("classId") Long classId,
                     @Param("name") String name,
                     @Param("targetGrade") String targetGrade,
                     @Param("maxStudents") Integer maxStudents,
                     @Param("status") String status,
                     @Param("description") String description);

    int countClassTeacher(@Param("classId") Long classId, @Param("teacherId") Long teacherId);

    void deleteClassTeacher(@Param("classId") Long classId, @Param("teacherId") Long teacherId);

    // 학원 소속 강사 목록 조회
    List<ClassroomDetailResponse.TeacherInfo> findTeachersByAcademyId(@Param("academyId") Long academyId);

    List<ClassroomDetailResponse.StudentInfo> findStudentsByClassId(@Param("classId") Long classId);
    int countStudentsByClassId(@Param("classId") Long classId);
    int existsClassStudent(@Param("classId") Long classId, @Param("studentId") Long studentId);
    void insertClassStudent(@Param("classId") Long classId, @Param("studentId") Long studentId);
    void deleteClassStudent(@Param("classId") Long classId, @Param("studentId") Long studentId);

    // 해당 반의 기존 수강생 전체 비우기
    void deleteAllStudentsByClassId(@Param("classId") Long classId);
    // 셔틀에서 체크된 수강생 목록 일괄 등록
    void batchInsertStudents(@Param("classId") Long classId, @Param("studentIds") List<Long> studentIds);

    // 현재 반(classId)에 이미 배정된 학생은 제외하고 왼쪽 풀 조회
    List<ClassroomDetailResponse.StudentInfo> findStudentPoolByAcademyId(
            @Param("academyId") Long academyId,
            @Param("classId") Long classId);

    // 전반 처리를 위한 타 반 배정 기록 삭제
    void deleteOtherClassMapping(@Param("studentIds") List<Long> studentIds);

    /**
     * 학생 기본 정보 수정 (선생님 권한)
     */
    void updateStudentInfo(@Param("studentId") Long studentId,
                           @Param("name") String name,
                           @Param("email") String email,
                           @Param("phone") String phone);
}
