package com.example.EduPOP.domain.exam;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 자체 시험 정보 외 버튼 종류와 오답 개수 필요
 */
@Getter
@Setter
public class StudentExam {

    private Long examId;

    private String title;
    private String examType;

    private LocalDate examDate;

    private Integer wrongCount;
}