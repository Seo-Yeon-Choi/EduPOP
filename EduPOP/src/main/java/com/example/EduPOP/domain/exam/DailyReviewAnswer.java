package com.example.EduPOP.domain.exam;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DailyReviewAnswer {

    private Long dailyReviewAnswerId;
    private Long dailyReviewAttemptId;
    private Long questionId;

    private String studentAnswer;

    private Boolean isCorrect;

    private BigDecimal earnedScore;

    private ExamQuestion question;
}