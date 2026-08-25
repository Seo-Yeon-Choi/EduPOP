package com.example.EduPOP.domain.exam;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class DailyReviewAttempt {

    private Long dailyReviewAttemptId;

    private Long studentId;

    private LocalDate reviewDate;

    private String status;

    private BigDecimal totalScore;
    private BigDecimal maxScore;

    private Integer correctCount;
    private Integer totalQuestionCount;

    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime gradedAt;
}