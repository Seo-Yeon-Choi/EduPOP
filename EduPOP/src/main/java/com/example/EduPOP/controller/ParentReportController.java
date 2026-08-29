package com.example.EduPOP.controller;

import com.example.EduPOP.controller.analytics.dto.StudentTrendResponse.RadarStatDto;
import com.example.EduPOP.domain.report.ParentReport;
import com.example.EduPOP.service.report.ParentReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
public class ParentReportController {

    private final ParentReportService parentReportService;

    // =========================================================
    // 생성자 주입
    // =========================================================

    public ParentReportController(
            ParentReportService parentReportService
    ) {

        this.parentReportService =
                parentReportService;
    }


    /**
     * =========================================================
     * 학부모 리포트 상세 페이지
     *
     * 기존 URL 변경 없음
     *
     * /parent/reports/{reportId}
     * =========================================================
     */

    @GetMapping("/parent/reports/{reportId}")
    public String showParentReport(
            @PathVariable Long reportId,
            Model model
    ) {


        // =====================================================
        // 1. 기존 학부모 리포트 조회
        //
        // access_token / 링크 / 발행 상태 등
        // 기존 기능은 건드리지 않는다.
        // =====================================================

        ParentReport report =
                parentReportService.getReport(
                        reportId
                );


        // =====================================================
        // 2. 리포트 존재 여부 확인
        // =====================================================

        if (report == null) {

            throw new IllegalArgumentException(
                    "존재하지 않는 리포트 번호입니다. (ID: "
                            + reportId
                            + ")"
            );
        }


        // =====================================================
        // 3. 기존 학부모 리포트 데이터 전달
        // =====================================================

        model.addAttribute(
                "report",
                report
        );


        // =====================================================
        // 4. 기존 점수 추이 데이터
        //
        // 기존 코드는 그대로 유지한다.
        // =====================================================

        Map<String, List<?>> trendData =
                parentReportService.getScoreTrend(
                        report.getStudentId()
                );


        model.addAttribute(
                "chartLabels",
                trendData.get("chartLabels")
        );

        model.addAttribute(
                "studentScores",
                trendData.get("studentScores")
        );

        model.addAttribute(
                "classScores",
                trendData.get("classScores")
        );


        // =====================================================
        // ⭐ 5. 월간 영역별 성취도 조회
        //
        // 핵심:
        //
        // 현재 학부모 리포트의 기간을 사용한다.
        //
        // 예:
        //
        // report.periodStart = 2026-08-01
        // report.periodEnd   = 2026-08-28
        //
        // 이 기간에 포함되는 시험만 가져온다.
        // =====================================================

        List<RadarStatDto> radarStats =
                parentReportService.getRadarStats(report);


        // =====================================================
        // 6. Thymeleaf에서 사용하기 위해 전달
        //
        // 기존 report 객체를 변경하지 않는다.
        //
        // 별도의 radarStats 속성으로 전달한다.
        // =====================================================

        model.addAttribute(
                "radarStats",
                radarStats != null
                        ? radarStats
                        : Collections.emptyList()
        );


        // =====================================================
        // 7. 기존 학부모 리포트 화면
        // =====================================================

        return "parent";
    }
}
