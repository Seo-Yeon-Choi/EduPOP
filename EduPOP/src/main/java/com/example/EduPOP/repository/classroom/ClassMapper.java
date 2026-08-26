package com.example.EduPOP.repository.classroom;

import com.example.EduPOP.domain.user.AcademyClass;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ClassMapper {

    List<AcademyClass> findByTeacherId(
            @Param("teacherId") Long teacherId
    );
}