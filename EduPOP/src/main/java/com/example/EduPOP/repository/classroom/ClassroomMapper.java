package com.example.EduPOP.repository.classroom;

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
    int updateStatus(@Param("classId") Long classId, @Param("status") String status);

    // 반 다중 일괄 상태 변경
    int updateStatusesBulk(@Param("classIds") List<Long> classIds, @Param("status") String status);
}
