package com.example.EduPOP.service.analytics;

import com.example.EduPOP.controller.analytics.dto.StudentTrendResponse;
import com.example.EduPOP.controller.analytics.dto.StudentTrendResponse.ExamHistoryDto;
import com.example.EduPOP.controller.analytics.dto.StudentTrendResponse.RadarStatDto;
import com.example.EduPOP.controller.analytics.dto.StudentTrendResponse.SubCategoryStatDto;
import com.example.EduPOP.repository.analytics.AnalyticsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * [역할: 성적 통계 가공 및 차트 데이터 생성 서비스]
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    // MyBatis Mapper 의존성 주입
    private final AnalyticsMapper analyticsMapper;

    /**
     * [비즈니스 메서드]
     * 학생 PK(user_id)를 받아 실제 DB에서 프로필, 회차별 점수, 영역별 밸런스를 조회
     */
    @Transactional(readOnly = true)
    public StudentTrendResponse getStudentTrendData(Long studentId) {
        log.info("DB에서 학생 user_id [{}]의 성적 분석 데이터를 조회합니다.", studentId);

        // 1. users + class_members + exam_attempts 조인 요약 정보 조회
        StudentTrendResponse response = analyticsMapper.findStudentSummary(studentId);

        // DB에 아직 학생 정보가 없거나 조회가 비어있을 때를 대비한 널 안전(Null-Safe) 방어 코드
        if (response == null) {
            response = new StudentTrendResponse();
            response.setStudentId(studentId);
            response.setStudentName("학생 " + studentId);
            response.setClassName("미배정");
            response.setSchoolGrade("중2");
            response.setRecentAverageScore(0.0);
            response.setAttendanceRate(100.0);
        }

        // 2. 실제 회차별 시험 점수 이력 조회
        List<ExamHistoryDto> histories = analyticsMapper.findExamHistoriesByStudentId(studentId);
        response.setExamHistories(histories != null ? histories : new ArrayList<>());

        // 3. 실제 영역별 정답률 통계 조회
        List<RadarStatDto> radarStats = analyticsMapper.findRadarStatsByStudentId(studentId);
        if (radarStats == null || radarStats.isEmpty()) {
            // 아직 푼 문제가 없는 경우 기본 3대 영역을 0%로 세팅
            radarStats = new ArrayList<>();
            radarStats.add(new RadarStatDto("VOCAB", 0.0, 0.0));
            radarStats.add(new RadarStatDto("GRAMMAR", 0.0, 0.0));
            radarStats.add(new RadarStatDto("READING", 0.0, 0.0));
        }
        response.setRadarStats(radarStats);

        // 4. 3단계 소분류 세부 성취도 통계 조회 및 Top 3 / Worst 3 가공 바인딩
        List<SubCategoryStatDto> subCategoryStats = analyticsMapper.findSubCategoryStatsByStudentId(studentId);

        if (subCategoryStats != null && !subCategoryStats.isEmpty()) {
            // 정답률 높은 순으로 정렬 후 상위 3개 (Top 3) 추출
            List<SubCategoryStatDto> top3 = subCategoryStats.stream()
                    .sorted((a, b) -> Double.compare(b.getStudentScoreRate(), a.getStudentScoreRate()))
                    .limit(3)
                    .collect(Collectors.toList());
            response.setTop3SubCategories(top3);

            // 정답률 낮은 순으로 정렬 후 하위 3개 (Worst 3 - 취약 영역) 추출
            List<SubCategoryStatDto> worst3 = subCategoryStats.stream()
                    .sorted((a, b) -> Double.compare(a.getStudentScoreRate(), b.getStudentScoreRate()))
                    .limit(3)
                    .collect(Collectors.toList());
            response.setWorst3SubCategories(worst3);
        } else {
            response.setTop3SubCategories(new ArrayList<>());
            response.setWorst3SubCategories(new ArrayList<>());
        }

        response.setSubCategoryStats(subCategoryStats != null ? subCategoryStats : new ArrayList<>());

        log.info("조회 완료: 학생명 [{}], 응시 이력 [{}건], 레이더 영역 [{}개]",
                response.getStudentName(), response.getExamHistories().size(), response.getRadarStats().size());

        return response;
    }
}