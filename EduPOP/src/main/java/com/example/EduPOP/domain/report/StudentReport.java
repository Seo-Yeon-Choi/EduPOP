package com.example.EduPOP.domain.report;

import com.vane.badwordfiltering.BadWordFiltering;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "student_reports")
public class StudentReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    // 리포트 대상 학생 ID
    private Long studentId;

    // 리포트 집계 기간
    private LocalDate periodStart;
    private LocalDate periodEnd;

    // =========================================================
    // 학생이 직접 입력하는 자아성찰 데이터
    // =========================================================

    private String pastResolution = "";
    private String proudestMoment = "";
    private String habitToImprove = "";
    private String selfFeedback = "";
    private String nextResolution = "";

    // 아는 개념 / 모르는 개념
    private String knownConcepts = "";
    private String unknownConcepts = "";

    // =========================================================
    // 감정 / 노력 만족도
    // =========================================================

    private String monthlyMood = "";
    private Integer selfEffortScore = 0;

    // =========================================================
    // 다른 팀원의 DB에서 자동 집계하는 행동 지표
    // =========================================================

    private Integer booksReadCount = 0;

    private Double examCompletionRate = 0.0;

    // 재시험 기준:
    // exam_attempts.attempt_no >= 2
    private Double retestCompletionRate = 0.0;

    private Integer studyAttendanceDays = 0;

    private Integer overcomeWrongCount = 0;

    @Transient
    private static final BadWordFiltering badWordFiltering =
            new BadWordFiltering();

    // JPA 기본 생성자
    protected StudentReport() {
    }

    // 새 리포트 생성용 생성자
    public StudentReport(
            Long studentId,
            LocalDate periodStart,
            LocalDate periodEnd
    ) {
        if (studentId == null) {
            throw new IllegalArgumentException(
                    "학생 ID는 필수입니다."
            );
        }

        if (periodStart == null || periodEnd == null) {
            throw new IllegalArgumentException(
                    "리포트 기간은 필수입니다."
            );
        }

        if (periodStart.isAfter(periodEnd)) {
            throw new IllegalArgumentException(
                    "리포트 시작일이 종료일보다 늦을 수 없습니다."
            );
        }

        this.studentId = studentId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
    }

    // 기존 코드 호환용 생성자
    public StudentReport(
            Long reportId,
            Long studentId,
            LocalDate periodStart,
            LocalDate periodEnd
    ) {
        this.reportId = reportId;
        this.studentId = studentId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
    }

    // =========================================================
    // 학생 입력 데이터 수정
    // =========================================================

    public void updateProudestMoment(
            String proudestMoment
    ) {
        this.proudestMoment =
                validateAndTrim(proudestMoment);
    }

    public void updateHabitToImprove(
            String habitToImprove
    ) {
        this.habitToImprove =
                validateAndTrim(habitToImprove);
    }

    public void updateSelfFeedback(
            String selfFeedback
    ) {
        this.selfFeedback =
                validateAndTrim(selfFeedback);
    }

    public void updateNextResolution(
            String nextResolution
    ) {
        this.nextResolution =
                validateAndTrim(nextResolution);
    }

    public void updateLearningConcepts(
            String knownConcepts,
            String unknownConcepts
    ) {
        this.knownConcepts =
                validateAndTrim(knownConcepts);

        this.unknownConcepts =
                validateAndTrim(unknownConcepts);
    }

    public void updateMoodAndScore(
            String monthlyMood,
            Integer selfEffortScore
    ) {
        if (selfEffortScore != null &&
                (selfEffortScore < 1 ||
                        selfEffortScore > 5)) {

            throw new IllegalArgumentException(
                    "노력 만족도는 1~5점이어야 합니다."
            );
        }

        this.monthlyMood =
                validateAndTrim(monthlyMood);

        this.selfEffortScore =
                selfEffortScore;
    }

    // 이전 리포트의 목표를 현재 리포트의 과거 목표로 연결
    public void setPastResolution(
            String pastResolution
    ) {
        this.pastResolution =
                validateAndTrim(pastResolution);
    }

    // =========================================================
    // 자동 집계 데이터 저장
    // =========================================================

    public void updateObjectiveMetrics(
            Integer booksReadCount,
            Double examCompletionRate,
            Double retestCompletionRate,
            Integer studyAttendanceDays,
            Integer overcomeWrongCount
    ) {
        this.booksReadCount =
                booksReadCount != null
                        ? booksReadCount
                        : 0;

        this.examCompletionRate =
                examCompletionRate != null
                        ? examCompletionRate
                        : 0.0;

        this.retestCompletionRate =
                retestCompletionRate != null
                        ? retestCompletionRate
                        : 0.0;

        this.studyAttendanceDays =
                studyAttendanceDays != null
                        ? studyAttendanceDays
                        : 0;

        this.overcomeWrongCount =
                overcomeWrongCount != null
                        ? overcomeWrongCount
                        : 0;
    }

    // =========================================================
    // 공통 문자열 검증
    // =========================================================

    private String validateAndTrim(
            String input
    ) {
        if (input == null) {
            return "";
        }

        String sanitized =
                input.trim();

        if (sanitized.length() > 1000) {
            throw new IllegalArgumentException(
                    "입력 내용이 1000자를 초과할 수 없습니다."
            );
        }

        return badWordFiltering.change(
                sanitized
        );
    }

    // =========================================================
    // Getter
    // =========================================================

    public Long getReportId() {
        return reportId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public String getPastResolution() {
        return pastResolution;
    }

    public String getProudestMoment() {
        return proudestMoment;
    }

    public String getHabitToImprove() {
        return habitToImprove;
    }

    public String getSelfFeedback() {
        return selfFeedback;
    }

    public String getNextResolution() {
        return nextResolution;
    }

    public String getKnownConcepts() {
        return knownConcepts;
    }

    public String getUnknownConcepts() {
        return unknownConcepts;
    }

    public String getMonthlyMood() {
        return monthlyMood;
    }

    public Integer getSelfEffortScore() {
        return selfEffortScore;
    }

    public Integer getBooksReadCount() {
        return booksReadCount;
    }

    public Double getExamCompletionRate() {
        return examCompletionRate;
    }

    public Double getRetestCompletionRate() {
        return retestCompletionRate;
    }

    public Integer getStudyAttendanceDays() {
        return studyAttendanceDays;
    }

    public Integer getOvercomeWrongCount() {
        return overcomeWrongCount;
    }

    // =========================================================
    // Setter
    // =========================================================

    public void setProudestMoment(
            String proudestMoment
    ) {
        updateProudestMoment(proudestMoment);
    }

    public void setHabitToImprove(
            String habitToImprove
    ) {
        updateHabitToImprove(habitToImprove);
    }

    public void setSelfFeedback(
            String selfFeedback
    ) {
        updateSelfFeedback(selfFeedback);
    }

    public void setNextResolution(
            String nextResolution
    ) {
        updateNextResolution(nextResolution);
    }

    public void setKnownConcepts(
            String knownConcepts
    ) {
        this.knownConcepts =
                validateAndTrim(knownConcepts);
    }

    public void setUnknownConcepts(
            String unknownConcepts
    ) {
        this.unknownConcepts =
                validateAndTrim(unknownConcepts);
    }

    public void setMonthlyMood(
            String monthlyMood
    ) {
        this.monthlyMood =
                validateAndTrim(monthlyMood);
    }
}