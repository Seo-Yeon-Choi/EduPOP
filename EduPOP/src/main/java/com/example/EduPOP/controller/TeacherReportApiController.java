package com.example.EduPOP.controller;

import com.example.EduPOP.service.report.ParentReportService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

// 💡 @RestController는 HTML 화면을 찾지 않고, 글자(JSON 데이터)만 바로 응답해주는 컨트롤러입니다.
@RestController
public class TeacherReportApiController {

    private final ParentReportService parentReportService;

    public TeacherReportApiController(ParentReportService parentReportService) {
        this.parentReportService = parentReportService;
    }

    // 🚀 프론트엔드 팀원분이 자바스크립트(fetch나 axios)로 찌르게 될 API 주소입니다!
    @PostMapping("/api/teacher/reports/{reportId}/publish")
    public Map<String, Object> publishReport(
            @PathVariable Long reportId,
            HttpServletRequest request) {

        // 1. 현재 우리 서버의 기본 인터넷 주소(예: http://localhost:8080)를 자동으로 알아냅니다.
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();

        // 2. 서비스 로직을 호출해서 DB 상태를 바꾸고, 고유 링크(access_token이 포함된 주소)를 받아옵니다.
        String shareUrl = parentReportService.publishAndGetShareLink(reportId, baseUrl);

        // 3. 프론트엔드 팀원이 쓰기 편하게 JSON(Map) 형태로 묶어서 돌려줍니다.
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("shareUrl", shareUrl);

        return response;
    }
}