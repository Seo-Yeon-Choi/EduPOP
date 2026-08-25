package com.example.EduPOP.repository.exam;

import com.example.EduPOP.domain.exam.ExamSection;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ExamSectionMapper {

    int insert(ExamSection section);

    List<ExamSection> findByExamId(Long examId);

}
