package com.example.EduPOP.repository.analytics;

import com.example.EduPOP.controller.analytics.dto.StudentTrendResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AnalyticsMapper {
    StudentTrendResponse findStudentSummary(@Param("studentId") Long studentId);
    List<StudentTrendResponse.ExamHistoryDto> findExamHistoriesByStudentId(@Param("studentId") Long studentId);
    List<StudentTrendResponse.RadarStatDto> findRadarStatsByStudentId(@Param("studentId") Long studentId);
    // 소분류별 성취도 통계 조회 메서드
    List<StudentTrendResponse.SubCategoryStatDto> findSubCategoryStatsByStudentId(@Param("studentId") Long studentId);
    List<StudentTrendResponse.SubCategoryStatDto> findAllCategories();
}