package com.example.EduPOP.domain.exam;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class StudentExamResult {

    private Long attemptId;
    private Long examId;
    private Long dailyReviewAttemptId;

    private String examTitle;
    private ExamType examType;

    private String attemptType;

    private String resultType;

    private BigDecimal totalScore;
    private BigDecimal maxScore;

    private Integer correctCount;
    private Integer totalQuestionCount;

    private LocalDateTime submittedAt;
    private LocalDate resultDate;
}