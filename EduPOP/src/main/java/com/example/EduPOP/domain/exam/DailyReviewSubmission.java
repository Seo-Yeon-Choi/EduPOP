package com.example.EduPOP.domain.exam;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DailyReviewSubmission {

    private Long dailyReviewAttemptId;

    private List<DailyReviewAnswer> answers;
}