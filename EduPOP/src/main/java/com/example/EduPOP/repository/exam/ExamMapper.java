package com.example.EduPOP.repository.exam;

import com.example.EduPOP.domain.exam.Exam;
import com.example.EduPOP.domain.exam.StudentExam;
import com.example.EduPOP.domain.exam.StudentExamResult;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ExamMapper {

    int insert(Exam exam);

    Exam findById(Long examId);

    int update(Exam exam);

    int delete(Long examId);

    List<Exam> findAll();

    List<StudentExamResult> findDailyReviewResults(Long studentId);
}