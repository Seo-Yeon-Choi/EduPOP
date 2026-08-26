package com.example.EduPOP.controller.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentTrendResponse {

    // 학생 기본 정보
    private Long studentId;
    private String studentName;
    private String className;
    private String schoolGrade;
    private Double recentAverageScore;
    private Double attendanceRate;

    // 회차별 성적 추이 목록
    private List<ExamHistoryDto> examHistories;

    // 5대 영역 방사형 차트 통계
    private List<RadarStatDto> radarStats;

    private List<SubCategoryStatDto> subCategoryStats;

    private List<SubCategoryStatDto> top3SubCategories;

    private List<SubCategoryStatDto> worst3SubCategories;

    /**
     * 시험 회차별 점수 추이 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExamHistoryDto {
        private String examTitle;
        private Integer examRound;
        private Double studentScore;
        private Double maxScore;
        private Double classAverageScore;
        private String examDate;
    }

    /**
     *  영역 레이더 차트 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RadarStatDto {
        private String tag;
        private Double studentScoreRate;
        private Double classAvgScoreRate;
    }

    /**
     * 소분류별 상세 성취도 통계
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubCategoryStatDto {
        private String examTitle;
        private String largeCategory; // 대분류 (예: 독해)
        private String smallCategory; // 소분류 (예: 빈칸추론)
        private Double studentScoreRate; // 학생 정답률 (%)
    }
}