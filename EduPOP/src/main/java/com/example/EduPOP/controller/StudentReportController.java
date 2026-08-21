package com.example.EduPOP.controller;

import com.example.EduPOP.service.StudentReportService;
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
        // ★ 핵심: 이제 웨이터가 주방장에게 직접 데이터를 넘겨주며 일을 시킵니다!
        studentReportService.updateProudestMoment(reportId, request.getProudestMoment());
        return "칭찬하고 싶은 점이 진짜로 DB에 저장되었습니다!";
    }

    @PatchMapping("/{reportId}/habit-to-improve")
    public String updateHabitToImprove(@PathVariable Long reportId, @RequestBody HabitRequest request) {

        // ★ CCTV 설치: 화면에서 보낸 데이터가 여기까지 잘 왔는지 콘솔창에 찍어봅니다!
        System.out.println("★★★ 프론트에서 넘어온 글씨: " + request.getHabitToImprove());

        studentReportService.updateHabitToImprove(reportId, request.getHabitToImprove());
        return "고치고 싶은 점(Problem)이 DB에 저장되었습니다!";
    }

    @PatchMapping("/{reportId}/self-feedback")
    public String updateSelfFeedback(@PathVariable Long reportId, @RequestBody FeedbackRequest request) {
        studentReportService.updateSelfFeedback(reportId, request.getSelfFeedback());
        return "셀프 피드백(Feedback)이 DB에 저장되었습니다!";
    }

    @PatchMapping("/{reportId}/next-resolution")
    public String updateNextResolution(@PathVariable Long reportId, @RequestBody ResolutionRequest request) {
        studentReportService.updateNextResolution(reportId, request.getNextResolution());
        return "다음 달 목표(Try)가 DB에 저장되었습니다!";
    }

    // ==========================================
    // 2. 만족도 별점 & 기분 저장 API
    // ==========================================
    @PatchMapping("/{reportId}/mood-and-score")
    public String updateMoodAndScore(@PathVariable Long reportId, @RequestBody MoodScoreRequest request) {
        // ★ 웨이터가 드디어 주방장에게 진짜로 일을 시킵니다!
        studentReportService.updateMoodAndScore(reportId, request.getMonthlyMood(), request.getSelfEffortScore());
        return "기분과 만족도가 DB에 성공적으로 저장되었습니다.";
    }

    // ==========================================
    // 3. 아는 개념 & 모르는 개념 저장 API
    // ==========================================
    @PatchMapping("/{reportId}/learning-concepts")
    public String updateLearningConcepts(@PathVariable Long reportId, @RequestBody ConceptRequest request) {
        // ★ 주방장에게 두 가지 개념 데이터를 모두 넘겨줍니다!
        studentReportService.updateLearningConcepts(reportId, request.getKnownConcepts(), request.getUnknownConcepts());
        return "학습 개념이 DB에 성공적으로 저장되었습니다.";
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