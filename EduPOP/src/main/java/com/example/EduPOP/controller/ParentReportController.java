package com.example.EduPOP.controller;

import com.example.EduPOP.domain.report.ParentReport;
import com.example.EduPOP.service.report.ParentReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@Controller
public class ParentReportController {

    private final ParentReportService parentReportService;

    // 생성자 주입 (Spring 권장 방식)
    public ParentReportController(ParentReportService parentReportService) {
        this.parentReportService = parentReportService;
    }

    /**
     * 학부모 리포트 상세 페이지 조회
     * @param reportId 조회할 리포트의 PK
     * @param model 화면(Thymeleaf)에 전달할 데이터 객체
     */
    @GetMapping("/parent/reports/{reportId}")
    public String showParentReport(@PathVariable Long reportId, Model model) {

        // 1. DB에서 리포트 기본 및 스냅샷 데이터 조회
        ParentReport report = parentReportService.getReport(reportId);

        // 2. 리포트가 존재하지 않거나 접근 권한이 없을 경우 예외 처리
        if (report == null) {
            throw new IllegalArgumentException("존재하지 않는 리포트 번호입니다. (ID: " + reportId + ")");
        }
        model.addAttribute("report", report);

        // 3. 하단 꺾은선 차트용 과거 14개월치 전체 성적 데이터 조회
        Map<String, List<?>> trendData = parentReportService.getScoreTrend(report.getStudentId());

        // 차트 렌더링을 위해 분리된 리스트(라벨, 학생 점수, 반 평균)를 각각 화면에 전달
        model.addAttribute("chartLabels", trendData.get("chartLabels"));
        model.addAttribute("studentScores", trendData.get("studentScores"));
        model.addAttribute("classScores", trendData.get("classScores"));

        return "parent"; // parent.html 렌더링
    }
}