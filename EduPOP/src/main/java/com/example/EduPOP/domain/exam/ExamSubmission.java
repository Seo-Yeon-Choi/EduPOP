package com.example.EduPOP.domain.exam;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 학생이 제출할 데이터를 받는 Domain
 */

@Getter
@Setter
public class ExamSubmission {

    private Long attemptId;

    private List<ExamAnswer> answers;
}