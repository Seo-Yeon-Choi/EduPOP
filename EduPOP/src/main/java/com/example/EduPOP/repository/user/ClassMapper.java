package com.example.EduPOP.repository.user;

import com.example.EduPOP.domain.user.AcademyClass;
import com.example.EduPOP.domain.user.ClassDetail;
import com.example.EduPOP.domain.user.StudentInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ClassMapper {
//학급 조회
    //관리자 학원의 학급목록 조회
    List<AcademyClass> findClassesByAcademyId(
        @Param("academyId") Long academyId
    );

    //특정 학급 조회
    AcademyClass findClassById(
            @Param("classId") Long classId,
            @Param("academyId") Long academyId
    );

    //학급 상세 기본 정보
    ClassDetail findClassDetail(
            @Param("classId") Long classId,
            @Param("academyId") Long academyId
    );

    //담임교사 조회
    String findTeacherName(
            @Param("classId") Long classId,
            @Param("academyId") Long academyId
    );

    //학생명단 조회
    List<StudentInfo> findStudent(
            @Param("classId") Long classId,
            @Param("academyId") Long academyId
    );

//-------------------------------------------------------------
    //학급 수정
    void updateClass(AcademyClass classInfo);


//-------------------------------------------------------------
    //학급 삭제
    void deleteClass(
            @Param("classId") Long classId,
            @Param("academyId") Long academyId
    );

    // class_students 관계 삭제
    void deleteClassStudents(
            @Param("classId") Long classId
    );

    // class_teachers 관계 삭제
    void deleteClassTeachers(
            @Param("classId") Long classId
    );

}
