package com.example.EduPOP.controller;

import com.example.EduPOP.domain.report.StudentReport;
import com.example.EduPOP.service.report.StudentReportService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/student-reports")
public class StudentReportController {

    private final StudentReportService studentReportService;

    public StudentReportController(
            StudentReportService studentReportService
    ) {
        this.studentReportService =
                studentReportService;
    }

    // =========================================================
    // 월간 리포트 자동 생성
    //
    // 다른 팀원의 데이터를 모아
    // student_reports에 자동 저장한다.
    // =========================================================

    @PostMapping("/generate")
    public StudentReport generateReport(
            @RequestBody GenerateReportRequest request
    ) {

        return studentReportService.createMonthlyReport(
                request.getStudentId(),
                request.getPeriodStart(),
                request.getPeriodEnd()
        );
    }


    // =========================================================
    // 특정 리포트 조회
    // =========================================================

    @GetMapping("/{reportId}")
    public StudentReport getReport(
            @PathVariable Long reportId
    ) {

        return studentReportService.getReport(
                reportId
        );
    }


    // =========================================================
    // Keep
    // =========================================================

    @PatchMapping("/{reportId}/proudest-moment")
    public String updateProudestMoment(
            @PathVariable Long reportId,
            @RequestBody ProudestRequest request
    ) {

        studentReportService.updateProudestMoment(
                reportId,
                request.getProudestMoment()
        );

        return studentReportService
                .getReport(reportId)
                .getProudestMoment();
    }


    // =========================================================
    // Problem
    // =========================================================

    @PatchMapping("/{reportId}/habit-to-improve")
    public String updateHabitToImprove(
            @PathVariable Long reportId,
            @RequestBody HabitRequest request
    ) {

        studentReportService.updateHabitToImprove(
                reportId,
                request.getHabitToImprove()
        );

        return studentReportService
                .getReport(reportId)
                .getHabitToImprove();
    }


    // =========================================================
    // Self Feedback
    // =========================================================

    @PatchMapping("/{reportId}/self-feedback")
    public String updateSelfFeedback(
            @PathVariable Long reportId,
            @RequestBody FeedbackRequest request
    ) {

        studentReportService.updateSelfFeedback(
                reportId,
                request.getSelfFeedback()
        );

        return studentReportService
                .getReport(reportId)
                .getSelfFeedback();
    }


    // =========================================================
    // Try
    // =========================================================

    @PatchMapping("/{reportId}/next-resolution")
    public String updateNextResolution(
            @PathVariable Long reportId,
            @RequestBody ResolutionRequest request
    ) {

        studentReportService.updateNextResolution(
                reportId,
                request.getNextResolution()
        );

        return studentReportService
                .getReport(reportId)
                .getNextResolution();
    }


    // =========================================================
    // 기분 / 만족도
    // =========================================================

    @PatchMapping("/{reportId}/mood-and-score")
    public MoodScoreRequest updateMoodAndScore(
            @PathVariable Long reportId,
            @RequestBody MoodScoreRequest request
    ) {

        studentReportService.updateMoodAndScore(
                reportId,
                request.getMonthlyMood(),
                request.getSelfEffortScore()
        );

        StudentReport report =
                studentReportService.getReport(
                        reportId
                );

        MoodScoreRequest response =
                new MoodScoreRequest();

        response.setMonthlyMood(
                report.getMonthlyMood()
        );

        response.setSelfEffortScore(
                report.getSelfEffortScore()
        );

        return response;
    }


    // =========================================================
    // 아는 개념 / 모르는 개념
    // =========================================================

    @PatchMapping("/{reportId}/learning-concepts")
    public ConceptRequest updateLearningConcepts(
            @PathVariable Long reportId,
            @RequestBody ConceptRequest request
    ) {

        studentReportService.updateLearningConcepts(
                reportId,
                request.getKnownConcepts(),
                request.getUnknownConcepts()
        );

        StudentReport report =
                studentReportService.getReport(
                        reportId
                );

        ConceptRequest response =
                new ConceptRequest();

        response.setKnownConcepts(
                report.getKnownConcepts()
        );

        response.setUnknownConcepts(
                report.getUnknownConcepts()
        );

        return response;
    }


    // =========================================================
    // DTO
    // =========================================================

    public static class GenerateReportRequest {

        private Long studentId;
        private LocalDate periodStart;
        private LocalDate periodEnd;

        public Long getStudentId() {
            return studentId;
        }

        public void setStudentId(
                Long studentId
        ) {
            this.studentId = studentId;
        }

        public LocalDate getPeriodStart() {
            return periodStart;
        }

        public void setPeriodStart(
                LocalDate periodStart
        ) {
            this.periodStart = periodStart;
        }

        public LocalDate getPeriodEnd() {
            return periodEnd;
        }

        public void setPeriodEnd(
                LocalDate periodEnd
        ) {
            this.periodEnd = periodEnd;
        }
    }


    public static class ProudestRequest {

        private String proudestMoment;

        public String getProudestMoment() {
            return proudestMoment;
        }

        public void setProudestMoment(
                String proudestMoment
        ) {
            this.proudestMoment = proudestMoment;
        }
    }


    public static class HabitRequest {

        private String habitToImprove;

        public String getHabitToImprove() {
            return habitToImprove;
        }

        public void setHabitToImprove(
                String habitToImprove
        ) {
            this.habitToImprove =
                    habitToImprove;
        }
    }


    public static class FeedbackRequest {

        private String selfFeedback;

        public String getSelfFeedback() {
            return selfFeedback;
        }

        public void setSelfFeedback(
                String selfFeedback
        ) {
            this.selfFeedback =
                    selfFeedback;
        }
    }


    public static class ResolutionRequest {

        private String nextResolution;

        public String getNextResolution() {
            return nextResolution;
        }

        public void setNextResolution(
                String nextResolution
        ) {
            this.nextResolution =
                    nextResolution;
        }
    }


    public static class MoodScoreRequest {

        private String monthlyMood;
        private Integer selfEffortScore;

        public String getMonthlyMood() {
            return monthlyMood;
        }

        public void setMonthlyMood(
                String monthlyMood
        ) {
            this.monthlyMood =
                    monthlyMood;
        }

        public Integer getSelfEffortScore() {
            return selfEffortScore;
        }

        public void setSelfEffortScore(
                Integer selfEffortScore
        ) {
            this.selfEffortScore =
                    selfEffortScore;
        }
    }


    public static class ConceptRequest {

        private String knownConcepts;
        private String unknownConcepts;

        public String getKnownConcepts() {
            return knownConcepts;
        }

        public void setKnownConcepts(
                String knownConcepts
        ) {
            this.knownConcepts =
                    knownConcepts;
        }

        public String getUnknownConcepts() {
            return unknownConcepts;
        }

        public void setUnknownConcepts(
                String unknownConcepts
        ) {
            this.unknownConcepts =
                    unknownConcepts;
        }
    }
}