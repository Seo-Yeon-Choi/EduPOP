package com.example.EduPOP.dto;

import lombok.Data;

@Data
public class ReportMetricsDTO {

    // =========================================================
    // 기본 정보
    // =========================================================

    private String studentName;

    private String academyName;

    private String className;

    private String teacherName;


    // =========================================================
    // 성과 지표
    // =========================================================

    // 이번 달 월말평가 점수
    private Double monthlyExamScore;

    // 해당 월말평가의 반 평균
    private Double classAverageScore;


    // =========================================================
    // 학습 태도 및 달성 지표
    // =========================================================

    // 단어 시험 응시율
    private Double wordExamCompletionRate;

    // 실제 제출된 독서감상문 수
    private Integer booksReadCount;

    // OVERCOME_WRONG 활동 횟수
    private Integer overcomeWrongCount;

    // 해당 월 정답률이 가장 낮은 문제 유형 1개
    private String topWeakTypeTag;
}
