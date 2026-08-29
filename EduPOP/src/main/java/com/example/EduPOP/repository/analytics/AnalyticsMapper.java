package com.example.EduPOP.repository.analytics;

import com.example.EduPOP.controller.analytics.dto.ClassWarningResponse;
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

    //  해당 반의 시험별 평균 점수 vs 전체 반 평균 점수 비교
    List<ClassWarningResponse.ExamComparisonDto> findExamComparisonsByClassId(Long classId);

    //  해당 반의 전체 취약 유형 TOP 3 & 우수 유형 TOP 3
    List<StudentTrendResponse.SubCategoryStatDto> findClassCategoryStats(@Param("classId") Long classId, @Param("isWorst") boolean isWorst);

    //  우측 슬라이드 패널용: 해당 반 학생들의 오답률 높은 순(취약 학생 순위) 조회
    List<ClassWarningResponse.StudentVulnerableDto> findVulnerableStudentsByClassId(Long classId);

    //  특정 학생의 취약 단어 TOP 5 조회
    List<String> findTopWeakWordsByStudentId(Long studentId);

    //  특정 학생의 취약 유형 TOP 3 조회
    List<String> findTopWeakTypesByStudentId(Long studentId);
}