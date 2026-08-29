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

import java.time.LocalDate;
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

        } else {
            response.setTop3SubCategories(new ArrayList<>());
        }

        // 시험별 행을 Java에서 단순 정렬하지 않고, 같은 유형을 DB에서 합산한 WORST 3를 조회
        List<SubCategoryStatDto> worst3 =
                analyticsMapper.findWorst3SubCategoriesByStudentId(studentId);

        response.setWorst3SubCategories(
                worst3 != null ? worst3 : new ArrayList<>()
        );

        response.setSubCategoryStats(subCategoryStats != null ? subCategoryStats : new ArrayList<>());

        log.info("조회 완료: 학생명 [{}], 응시 이력 [{}건], 레이더 영역 [{}개]",
                response.getStudentName(), response.getExamHistories().size(), response.getRadarStats().size());

        return response;
    }
    // =========================================================
    // ⭐ [추가]
    // 기간 제한 영역별 성취도
    // 사용 목적:학부모 리포트의 "월간 영역별 성취도"
    // ---------------------------------------------------------
    // 기존 student-trend의 영역별 성취도와 동일한 SQL 계산식 사용
    // 단, periodStart <= 시험일 <= periodEnd 조건을 추가해서 해당 월의 시험만 계산한다.
    // =========================================================

    @Transactional(readOnly = true)
    public List<RadarStatDto> getRadarStatsByPeriod(
            Long studentId,
            LocalDate periodStart,
            LocalDate periodEnd
    ) {

        // -----------------------------------------------------
        // 기본 파라미터 검증
        // -----------------------------------------------------

        if (studentId == null) {

            throw new IllegalArgumentException(
                    "학생 ID가 없습니다."
            );
        }


        if (
                periodStart == null
                        || periodEnd == null
        ) {

            throw new IllegalArgumentException(
                    "조회 기간이 없습니다."
            );
        }


        if (
                periodStart.isAfter(
                        periodEnd
                )
        ) {

            throw new IllegalArgumentException(
                    "조회 시작일이 종료일보다 늦을 수 없습니다."
            );
        }


        log.info(
                "학생 [{}]의 기간별 영역 성취도 조회: {} ~ {}",
                studentId,
                periodStart,
                periodEnd
        );


        // -----------------------------------------------------
        // ⭐ 실제 DB 조회
        // 기존 radarStats와 동일한 계산식이지만 기간을 함께 전달한다.
        // -----------------------------------------------------

        List<RadarStatDto> radarStats =
                analyticsMapper
                        .findRadarStatsByStudentIdAndPeriod(
                                studentId,
                                periodStart,
                                periodEnd
                        );


        // -----------------------------------------------------
        // 데이터가 없으면 빈 리스트 반환
        // 학부모 리포트에서는 존재하지 않는 영역을 임의로 만들어내지 않는다.
        // =====================================================

        if (
                radarStats == null
                        || radarStats.isEmpty()
        ) {

            return new ArrayList<>();
        }


        return radarStats;
    }
}
