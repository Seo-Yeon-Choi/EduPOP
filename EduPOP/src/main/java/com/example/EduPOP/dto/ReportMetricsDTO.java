package com.example.EduPOP.dto;

import lombok.Data;

@Data
public class ReportMetricsDTO {
    // 1. 기본 정보
    private String studentName;
    private String academyName;
    private String className;
    private String teacherName;

    // 2. 성과 지표
    private Double monthlyExamScore;
    private Double classAverageScore;

    // (이제 영역별 점수는 다각형 차트 JSON 데이터가 대신합니다.)

    // 3. 학습 태도 지표
    private Double wordExamCompletionRate;
    private Integer booksReadCount;
    private Integer overcomeWrongCount;
    private String topWeakTypeTag;
}