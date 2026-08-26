package com.example.EduPOP.repository.exam;

import com.example.EduPOP.domain.exam.ExamQuestionChoice;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ExamQuestionChoiceMapper {

    int insert(ExamQuestionChoice choice);

    List<ExamQuestionChoice> findByQuestionId(Long questionId);

    int update(ExamQuestionChoice choice);

    int deleteByQuestionId(Long questionId);
}
