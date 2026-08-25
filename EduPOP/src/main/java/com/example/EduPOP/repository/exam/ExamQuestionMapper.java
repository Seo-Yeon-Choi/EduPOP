package com.example.EduPOP.repository.exam;

import com.example.EduPOP.domain.exam.ExamQuestion;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ExamQuestionMapper {

    int insert(ExamQuestion question);

    ExamQuestion findById(Long questionId);

    List<ExamQuestion> findByExamId(Long examId);

    int update(ExamQuestion question);

    int delete(Long questionId);

}
