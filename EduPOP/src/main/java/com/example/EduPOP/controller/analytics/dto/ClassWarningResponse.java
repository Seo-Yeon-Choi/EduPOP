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
public class ClassWarningResponse {

    private Long classId;
    private String className;

    private String warningLevel;
    private String warningReason;

    private List<ExamComparisonDto> examComparisons;
    private List<StudentTrendResponse.SubCategoryStatDto> classWorstCategories;
    private List<StudentTrendResponse.SubCategoryStatDto> classTopCategories;
    private List<StudentVulnerableDto> vulnerableStudents;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExamComparisonDto {
        private String examTitle;
        private String examDate;
        private Double classAverageScore;
        private Double totalAverageScore;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentVulnerableDto {
        private Long studentId;
        private String studentName;
        private Double errorRate;
        private List<String> topWeakWords;
        private List<String> topWeakTypes;
        // 추가: 윤서영이 만든 학부모 리포트의 ID를 담을 그릇
        private Long reportId;
    }
}