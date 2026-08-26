package com.example.EduPOP.domain.exam;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class StudentGrowthSummary {

    private LocalDate periodStart;
    private LocalDate periodEnd;

    private Integer previousAverageScore = 0;
    private Integer recentAverageScore = 0;
    private Integer scoreChange = 0;

    private Integer previousAttemptCount = 0;
    private Integer recentAttemptCount = 0;
    private Integer completedAttemptCount = 0;

    private Integer reviewCorrectCount = 0;
    private Integer reviewQuestionCount = 0;
    private Integer retrySuccessRate = 0;

    private Integer studyDays = 0;
    private Integer longestStreak = 0;
}
