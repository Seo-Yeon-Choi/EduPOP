package com.example.EduPOP.controller;

import com.example.EduPOP.controller.analytics.dto.StudentTrendResponse.RadarStatDto;
import com.example.EduPOP.domain.report.ParentReport;
import com.example.EduPOP.service.report.ParentReportService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class SharedReportController {

    private final ParentReportService parentReportService;

    public SharedReportController(ParentReportService parentReportService) {
        this.parentReportService = parentReportService;
    }

    @GetMapping("/share/reports/auth")
    public String classAuthPage(@RequestParam("classId") Long classId, Model model) {
        model.addAttribute("classId", classId);
        return "report-auth";
    }

    @PostMapping("/share/reports/auth/verify")
    public String verifyClassAuth(
            @RequestParam("classId") Long classId,
            @RequestParam("studentName") String studentName,
            @RequestParam("phoneLast4") String phoneLast4,
            HttpServletResponse response, // 🚀 추가: 쿠키를 굽기 위해 필요!
            Model model) {

        String reportToken = parentReportService.authenticateAndGetReportToken(classId, studentName, phoneLast4);

        if (reportToken != null) {
            // 🚀 [핵심 수정] 인증 성공 시, DB에 기기를 등록하고 쿠키를 구워줍니다!
            ParentReport report = parentReportService.getReportByToken(reportToken);
            String newDeviceToken = parentReportService.registerDeviceLink(report.getStudentId());

            Cookie deviceCookie = new Cookie("EDUPOP_DEVICE", newDeviceToken);
            deviceCookie.setMaxAge(60 * 60 * 24 * 365); // 1년
            deviceCookie.setPath("/");
            response.addCookie(deviceCookie);

            // 발급된 토큰이 있는 진짜 리포트 주소로 리다이렉트
            return "redirect:/share/reports/" + reportToken;
        } else {
            model.addAttribute("classId", classId);
            model.addAttribute("error", "❌ 학생 이름이나 전화번호가 일치하지 않거나, 아직 발행된 리포트가 없습니다.");
            return "report-auth";
        }
    }

    @GetMapping("/share/reports/{token}")
    public String viewSharedReport(
            @PathVariable String token,
            @CookieValue(value = "EDUPOP_DEVICE", required = false) String deviceToken,
            Model model) {

        ParentReport report = parentReportService.getReportByToken(token);
        if (report == null) throw new IllegalArgumentException("존재하지 않는 링크입니다.");

        boolean isLinkedDevice = parentReportService.isValidDevice(report.getStudentId(), deviceToken);

        if (!isLinkedDevice) {
            model.addAttribute("token", token);
            return "verify";
        }

        model.addAttribute("report", report);
        Map<String, List<?>> trendData = parentReportService.getScoreTrend(report.getStudentId());
        model.addAttribute("chartLabels", trendData.get("chartLabels"));
        model.addAttribute("studentScores", trendData.get("studentScores"));
        model.addAttribute("classScores", trendData.get("classScores"));

        // 직접 상세 화면과 동일한 영역별 성취도 데이터를 공유 화면에도 전달
        List<RadarStatDto> radarStats =
                parentReportService.getRadarStats(report);

        model.addAttribute("radarStats", radarStats);

        return "parent";
    }

    @PostMapping("/share/reports/{token}/verify")
    public String verifyAndBakeCookie(
            @PathVariable String token,
            @RequestParam String studentName,
            @RequestParam String phoneLast4,
            HttpServletResponse response,
            Model model) {

        ParentReport report = parentReportService.getReportByToken(token);
        boolean isCorrect = parentReportService.verifyParentAuth(report.getStudentId(), studentName, phoneLast4);

        if (isCorrect) {
            String newDeviceToken = parentReportService.registerDeviceLink(report.getStudentId());

            Cookie deviceCookie = new Cookie("EDUPOP_DEVICE", newDeviceToken);
            deviceCookie.setMaxAge(60 * 60 * 24 * 365);
            deviceCookie.setPath("/");
            response.addCookie(deviceCookie);

            return "redirect:/share/reports/" + token;
        } else {
            model.addAttribute("token", token);
            model.addAttribute("error", "❌ 정보가 일치하지 않습니다.");
            return "verify";
        }
    }
}
