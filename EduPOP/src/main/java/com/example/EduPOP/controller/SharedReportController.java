package com.example.EduPOP.controller;

import com.example.EduPOP.domain.report.ParentReport;
import com.example.EduPOP.service.ParentReportService;
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

    @GetMapping("/share/reports/{token}")
    public String viewSharedReport(
            @PathVariable String token,
            @CookieValue(value = "EDUPOP_DEVICE", required = false) String deviceToken, // 쿠키 이름 변경
            Model model) {

        ParentReport report = parentReportService.getReportByToken(token);
        if (report == null) throw new IllegalArgumentException("존재하지 않는 링크입니다.");

        // 🚀 [핵심] 쿠키에 있는 토큰이 DB에 등록된 유효한 기기인지 깐깐하게 검사!
        boolean isLinkedDevice = parentReportService.isValidDevice(report.getStudentId(), deviceToken);

        if (!isLinkedDevice) {
            model.addAttribute("token", token);
            return "verify"; // 기기 등록이 안 되어있으면 인증 화면으로!
        }

        // 유효한 기기면 리포트 통과!
        model.addAttribute("report", report);
        Map<String, List<?>> trendData = parentReportService.getScoreTrend(report.getStudentId());
        model.addAttribute("chartLabels", trendData.get("chartLabels"));
        model.addAttribute("studentScores", trendData.get("studentScores"));
        model.addAttribute("classScores", trendData.get("classScores"));
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
            // 🚀 [핵심] 인증 합격 시, DB에 기기를 등록하고 안전한 고유 토큰을 발급받습니다!
            String newDeviceToken = parentReportService.registerDeviceLink(report.getStudentId());

            Cookie deviceCookie = new Cookie("EDUPOP_DEVICE", newDeviceToken);
            deviceCookie.setMaxAge(60 * 60 * 24 * 365); // 1년
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