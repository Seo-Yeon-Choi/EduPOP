package com.example.EduPOP.controller.exam.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * [역할: 시험 서식(템플릿) 선택용 DTO - ERD EXAMS 테이블과 1:1 매핑]
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamTemplateResponse {
    private Long examId;              // EXAMS.exam_id
    private String title;             // EXAMS.title
    private String examType;          // EXAMS.exam_type (WORD, MONTH 등)
    private Double totalScore;        // EXAM_QUESTIONS score 합계
    private Integer totalQuestions;   // EXAM_QUESTIONS 문항 개수
}