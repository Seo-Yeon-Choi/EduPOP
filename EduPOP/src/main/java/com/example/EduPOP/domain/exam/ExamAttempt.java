package com.example.EduPOP.domain.exam;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ExamAttempt {

    private Long attemptId;
    private Long examId;
    private Long studentId;

    private Integer attemptNo;

    private String attemptType;
    private Long sourceAttemptId;

    private String entryMethod;
    private String status;

    private BigDecimal totalScore;
    private BigDecimal maxScore;

    private Integer correctCount;
    private Integer totalQuestionCount;

    private String primaryWeakTag;

    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime gradedAt;
}