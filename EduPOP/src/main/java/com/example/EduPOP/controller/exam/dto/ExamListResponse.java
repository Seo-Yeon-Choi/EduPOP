package com.example.EduPOP.controller.exam.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * [역할: 시험 목록 한 행(Row) 데이터를 담는 DTO]
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExamListResponse {
    private Long examId;              // 시험 PK
    private Long classId;
    private Integer examRound;        // 회차 (예: 1회차)
    private String title;             // 시험명
    private String className;         // 배정 반 이름
    private String examDate;          // 시행 일자
    private Integer totalQuestions;   // 총 문항 수
    private Integer gradedStudentCount; // 채점 완료된 학생 수
}