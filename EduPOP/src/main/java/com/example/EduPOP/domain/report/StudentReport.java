package com.example.EduPOP.domain.report;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity // "이 클래스는 데이터베이스의 테이블과 1:1로 맵핑되는 녀석이야!"
@Table(name = "student_reports")
public class StudentReport {
    // === 필드 영역: 캡슐화를 위해 모두 private으로 선언 ===

    @Id // "이 변수가 테이블의 각 줄을 구분하는 기본키(Primary Key, 열쇠)야!"
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;
    private Long studentId;
    private LocalDate periodStart;
    private LocalDate periodEnd;

    // 회고 입력 항목 (기본값을 빈 문자열로 초기화)
    private String pastResolution = "";  // 과거 다짐 (이전 리포트에서 가져옴)
    private String proudestMoment = "";  // 칭찬하고 싶은 점 (Keep)
    private String habitToImprove = "";  // 고치고 싶은 점 (Problem)
    private String selfFeedback = "";    // 셀프 피드백
    private String nextResolution = "";  // 다음 달 목표 (Try)
    private String knownConcepts = "";   // 아는 개념
    private String unknownConcepts = ""; // 모르는 개념

    // 감정 및 만족도
    private String monthlyMood = "";     // 기분
    private Integer selfEffortScore = 0;    // 1~5점 별점
    // [2. 성찰의 객관적 근거: 나의 행동 지표] - 팀장님 스키마 추가!
    private Integer booksReadCount = 0;
    private Double examCompletionRate = 0.0;
    private Double retestCompletionRate = 0.0;
    private Integer studyAttendanceDays = 0;
    private Integer overcomeWrongCount = 0;

    // 3. JPA 조립을 위한 기본 생성자 추가!
    // (아무나 빈 깡통을 만들지 못하도록 protected로 살짝 막아두는 것이 실무 국룰입니다)
    protected StudentReport() {}

    // === 생성자 영역: 객체가 생성될 때 무결성(안전성)을 검증 ===
    public StudentReport(Long reportId, Long studentId, LocalDate periodStart, LocalDate periodEnd) {
        if (reportId == null) {
            throw new IllegalArgumentException("리포트 ID는 필수입니다.");
        }
        if (studentId == null) {
            throw new IllegalArgumentException("학생 ID는 필수입니다.");
        }
        if (periodStart == null || periodEnd == null) {
            throw new IllegalArgumentException("평가 기간은 필수입니다.");
        }
        if (periodStart.isAfter(periodEnd)) {
            throw new IllegalArgumentException("시작일이 종료일보다 늦을 수 없습니다.");
        }

        this.reportId = reportId;
        this.studentId = studentId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
    }

    // === 비즈니스 로직(메서드) 영역 ===

    /**
     * 1. 칭찬하고 싶은 점 개별 자동 저장
     */
    public void updateProudestMoment(String proudestMoment) {
        this.proudestMoment = validateAndTrim(proudestMoment);
    }

    /**
     * 2. 고치고 싶은 습관 개별 자동 저장
     */
    public void updateHabitToImprove(String habitToImprove) {
        this.habitToImprove = validateAndTrim(habitToImprove);
    }

    /**
     * 3. 셀프 피드백 개별 자동 저장
     */
    public void updateSelfFeedback(String selfFeedback) {
        this.selfFeedback = validateAndTrim(selfFeedback);
    }

    /**
     * 4. 다음 달 다짐 개별 자동 저장
     */
    public void updateNextResolution(String nextResolution) {
        this.nextResolution = validateAndTrim(nextResolution);
    }

    /**
     * 5. 아는 개념 / 모르는 개념 개별 업데이트
     */
    public void updateLearningConcepts(String knownConcepts, String unknownConcepts) {
        this.knownConcepts = validateAndTrim(knownConcepts);
        this.unknownConcepts = validateAndTrim(unknownConcepts);
    }

    /**
     * 6. 기분 상태와 노력 만족도(별점 1~5점) 업데이트
     */
    public void updateMoodAndScore(String monthlyMood, Integer selfEffortScore) { // 여기도 Integer!
        // null이 아닐 때만 1~5점 검사! (빈칸 저장을 허용함)
        if (selfEffortScore != null && (selfEffortScore < 1 || selfEffortScore > 5)) {
            throw new IllegalArgumentException("노력 만족도(별점)는 1점에서 5점 사이여야 합니다.");
        }
        this.monthlyMood = monthlyMood;
        this.selfEffortScore = selfEffortScore;
    }

    /**
     * 7. 이전 리포트의 다짐을 불러와서 세팅
     */
    public void setPastResolution(String pastResolution) {
        this.pastResolution = validateAndTrim(pastResolution);
    }

    /**
     * (공통 내부 헬퍼 메소드) 데이터 유효성을 검사하고 안전한 형태로 정제합니다.
     */
    private String validateAndTrim(String input) {
        if (input == null) {
            return "";
        }

        String sanitized = input.trim();

        if (sanitized.length() > 1000) {
            throw new IllegalArgumentException("입력 내용이 1000자를 초과할 수 없습니다.");
        }

        return sanitized;
    } // <- 아까 에러 났던 닫는 중괄호가 바로 이 자리입니다!

    // === 데이터를 꺼내 쓰기 위한 Getter 영역 ===
    public Long getReportId() { return reportId; }
    public Long getStudentId() { return studentId; }
    public LocalDate getPeriodStart() { return periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public String getPastResolution() { return pastResolution; }
    public String getProudestMoment() { return proudestMoment; }
    public String getHabitToImprove() { return habitToImprove; }
    public String getSelfFeedback() { return selfFeedback; }
    public String getNextResolution() { return nextResolution; }
    public String getKnownConcepts() { return knownConcepts; }
    public String getUnknownConcepts() { return unknownConcepts; }
    public String getMonthlyMood() { return monthlyMood; }
    public Integer getSelfEffortScore() { return selfEffortScore; }
    // === 팀장님이 추가하신 행동 지표 Getter ===
    public Integer getBooksReadCount() { return booksReadCount; }
    public Double getExamCompletionRate() { return examCompletionRate; }
    public Double getRetestCompletionRate() { return retestCompletionRate; }
    public Integer getStudyAttendanceDays() { return studyAttendanceDays; }
    public Integer getOvercomeWrongCount() { return overcomeWrongCount; }

    // ==========================================
    // 데이터 수정을 위한 Setter 메서드들 (수정 버튼)
    // ==========================================

    public void setHabitToImprove(String habitToImprove) {
        this.habitToImprove = habitToImprove;
    }

    public void setSelfFeedback(String selfFeedback) {
        this.selfFeedback = selfFeedback;
    }

    public void setNextResolution(String nextResolution) {
        this.nextResolution = nextResolution;
    }
}