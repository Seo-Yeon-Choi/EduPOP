package com.example.EduPOP.controller;

import com.example.EduPOP.domain.report.StudentReport;
import com.example.EduPOP.service.report.StudentReportService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student-reports")
public class StudentReportController {

    // TODO: 나중에 주방장(StudentReportService)을 연결할 자리
    // 1. 웨이터가 일할 때 필요한 주방장(Service)을 final 변수로 선언
    private final StudentReportService studentReportService;

    // 2. [생성자 주입] 스프링이 알아서 만들어둔 StudentReportService 주방장을 이곳에 꽂아줍니다!
    public StudentReportController(StudentReportService studentReportService) {
        this.studentReportService = studentReportService;
    }

    // ==========================================
    // 1. 이번 달 나의 회고 (4가지 항목) 자동 저장 API
    // ==========================================

    @PatchMapping("/{reportId}/proudest-moment")
    public String updateProudestMoment(@PathVariable Long reportId, @RequestBody ProudestRequest request) {
        // 1. 주방장에게 저장을 시킵니다 (이때 *** 필터링 작동!)
        studentReportService.updateProudestMoment(reportId, request.getProudestMoment());

        // 2. 필터링된 깨끗한 데이터를 DB에서 다시 꺼내서 화면(JS)으로 던져줍니다!
        StudentReport report = studentReportService.getReport(reportId);
        return report.getProudestMoment();
    }

    @PatchMapping("/{reportId}/habit-to-improve")
    public String updateHabitToImprove(@PathVariable Long reportId, @RequestBody HabitRequest request) {
        studentReportService.updateHabitToImprove(reportId, request.getHabitToImprove());

        StudentReport report = studentReportService.getReport(reportId);
        return report.getHabitToImprove();
    }

    @PatchMapping("/{reportId}/self-feedback")
    public String updateSelfFeedback(@PathVariable Long reportId, @RequestBody FeedbackRequest request) {
        studentReportService.updateSelfFeedback(reportId, request.getSelfFeedback());

        StudentReport report = studentReportService.getReport(reportId);
        return report.getSelfFeedback();
    }

    @PatchMapping("/{reportId}/next-resolution")
    public String updateNextResolution(@PathVariable Long reportId, @RequestBody ResolutionRequest request) {
        studentReportService.updateNextResolution(reportId, request.getNextResolution());

        StudentReport report = studentReportService.getReport(reportId);
        return report.getNextResolution();
    }

    // ==========================================
    // 2. 만족도 별점 & 기분 저장 API (비속어 필터링 적용 반환)
    // ==========================================
    @PatchMapping("/{reportId}/mood-and-score")
    public MoodScoreRequest updateMoodAndScore(@PathVariable Long reportId, @RequestBody MoodScoreRequest request) {
        // 1. 주방장에게 저장을 시킵니다 (이때 기분에 비속어가 있으면 *** 처리됨)
        studentReportService.updateMoodAndScore(reportId, request.getMonthlyMood(), request.getSelfEffortScore());

        // 2. DB에서 필터링이 끝난 최신 리포트를 다시 꺼내옵니다.
        StudentReport report = studentReportService.getReport(reportId);

        // 3. 화면(JS)이 알아먹기 쉽게 다시 DTO 상자에 담아서(JSON) 던져줍니다!
        MoodScoreRequest response = new MoodScoreRequest();
        response.setMonthlyMood(report.getMonthlyMood());
        response.setSelfEffortScore(report.getSelfEffortScore());

        return response; // 스프링이 알아서 JSON 형태로 변환해 줍니다!
    }

    // ==========================================
    // 3. 아는 개념 & 모르는 개념 저장 API (비속어 필터링 적용 반환)
    // ==========================================
    @PatchMapping("/{reportId}/learning-concepts")
    public ConceptRequest updateLearningConcepts(@PathVariable Long reportId, @RequestBody ConceptRequest request) {
        // 1. 주방장에게 저장을 시킵니다 (이때 비속어가 있으면 *** 처리됨)
        studentReportService.updateLearningConcepts(reportId, request.getKnownConcepts(), request.getUnknownConcepts());

        // 2. DB에서 최신 리포트 꺼내오기
        StudentReport report = studentReportService.getReport(reportId);

        // 3. 화면에 돌려줄 DTO 상자에 안전한(***) 데이터 담기
        ConceptRequest response = new ConceptRequest();
        response.setKnownConcepts(report.getKnownConcepts());
        response.setUnknownConcepts(report.getUnknownConcepts());

        return response;
    }


    // ==========================================
    // 📦 프론트엔드에서 날아오는 JSON 데이터를 담을 빈 상자들 (DTO 클래스)
    // ==========================================

    static class ProudestRequest {
        private String proudestMoment;
        public String getProudestMoment() { return proudestMoment; }
        public void setProudestMoment(String proudestMoment) { this.proudestMoment = proudestMoment; }
    }

    static class HabitRequest {
        private String habitToImprove;
        public String getHabitToImprove() { return habitToImprove; }
        public void setHabitToImprove(String habitToImprove) { this.habitToImprove = habitToImprove; }
    }

    static class FeedbackRequest {
        private String selfFeedback;
        public String getSelfFeedback() { return selfFeedback; }
        public void setSelfFeedback(String selfFeedback) { this.selfFeedback = selfFeedback; }
    }

    static class ResolutionRequest {
        private String nextResolution;
        public String getNextResolution() { return nextResolution; }
        public void setNextResolution(String nextResolution) { this.nextResolution = nextResolution; }
    }

    static class MoodScoreRequest {
        private String monthlyMood;
        private Integer selfEffortScore; // 빈칸(null) 허용

        public String getMonthlyMood() { return monthlyMood; }
        public void setMonthlyMood(String monthlyMood) { this.monthlyMood = monthlyMood; }

        // ⭐️ 리턴 타입도 똑같이 Integer로 변경완료!
        public Integer getSelfEffortScore() { return selfEffortScore; }
        // ⭐️ Setter(수정 버튼) 추가완료!
        public void setSelfEffortScore(Integer selfEffortScore) { this.selfEffortScore = selfEffortScore; }
    }

    static class ConceptRequest {
        private String knownConcepts;
        private String unknownConcepts;

        public String getKnownConcepts() { return knownConcepts; }
        public void setKnownConcepts(String knownConcepts) { this.knownConcepts = knownConcepts; } // ⭐️ 추가!

        public String getUnknownConcepts() { return unknownConcepts; }
        public void setUnknownConcepts(String unknownConcepts) { this.unknownConcepts = unknownConcepts; } // ⭐️ 추가!
    }
}