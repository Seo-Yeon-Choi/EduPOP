package com.example.EduPOP.repository.classroom;

import com.example.EduPOP.domain.classroom.ClassTeacher;
import com.example.EduPOP.domain.classroom.Classroom;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 반 관리 및 배정 매퍼 인터페이스 (MyBatis)
 * - classes 및 class_teachers 테이블 대상 데이터 접근 계층
 */
@Mapper
public interface ClassroomMapper {

    // 반 등록 (PK 자동 생성)
    int insertClass(Classroom classroom);

    // 반-강사 배정 매핑 등록
    int insertClassTeacher(ClassTeacher classTeacher);

    // 같은 학원 내 반 이름 중복 조회
    Classroom findByNameAndAcademyId(@Param("name") String name,
                                     @Param("academyId") Long academyId);
}
