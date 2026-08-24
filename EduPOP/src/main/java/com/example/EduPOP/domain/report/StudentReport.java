package com.example.EduPOP.domain.report;

import com.vane.badwordfiltering.BadWordFiltering;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity // "이 클래스는 데이터베이스의 'student_reports' 테이블과 1:1로 맵핑되는 설계도야!"
@Table(name = "student_reports")
public class StudentReport {

    // ==========================================
    // 🗄️ 필드 영역: 데이터를 안전하게 보호하기 위해 모두 private으로 선언
    // ==========================================

    @Id // "이 변수가 테이블의 각 줄을 구분하는 기본키(Primary Key, PK)야!"
    @GeneratedValue(strategy = GenerationType.IDENTITY) // "DB가 알아서 1, 2, 3... 번호를 자동으로 매겨줘! (AUTO_INCREMENT)"
    private Long reportId;

    private Long studentId; // 리포트의 주인 (유저 ID)
    private LocalDate periodStart; // 4주 리포트 시작일
    private LocalDate periodEnd;   // 4주 리포트 종료일

    // [1. 회고 입력 항목] (Null 방지를 위해 빈 문자열로 초기화해 두는 실무 센스!)
    private String pastResolution = "";  // 과거 다짐 (이전 리포트에서 가져옴)
    private String proudestMoment = "";  // 칭찬하고 싶은 점 (Keep)
    private String habitToImprove = "";  // 고치고 싶은 점 (Problem)
    private String selfFeedback = "";    // 셀프 피드백
    private String nextResolution = "";  // 다음 달 목표 (Try)
    private String knownConcepts = "";   // 아는 개념
    private String unknownConcepts = ""; // 모르는 개념

    // [2. 감정 및 만족도]
    private String monthlyMood = "";     // 기분
    private Integer selfEffortScore = 0; // 1~5점 별점

    // [3. 성찰의 객관적 근거: 행동 지표] (팀장님 스키마 반영)
    private Integer booksReadCount = 0;
    private Double examCompletionRate = 0.0;
    private Double retestCompletionRate = 0.0;
    private Integer studyAttendanceDays = 0;
    private Integer overcomeWrongCount = 0;

    // ==========================================
    // 🛡️ 필터링 객체 (중요!)
    // ==========================================
    // @Transient: "이 변수는 자바(메모리)에서만 쓸 거니까 DB 테이블의 컬럼으로 만들지 마!" 라는 뜻입니다.
    // static final: 검사할 때마다 사전을 새로 만들면 서버가 느려지니, 프로그램이 켜질 때 딱 1번만 만들어서 평생 재사용합니다.
    @Transient
    private static final BadWordFiltering badWordFiltering = new BadWordFiltering();

    // ==========================================
    // 🏗️ 생성자 영역
    // ==========================================
    // JPA(하이버네이트)가 데이터를 DB에서 꺼내서 조립할 때 쓸 '빈 깡통' 생성자가 반드시 필요합니다.
    // 외부에서 아무나 빈 객체를 만들지 못하게 protected로 막아두는 것이 실무 국룰입니다.
    protected StudentReport() {
    }

    // 처음 리포트가 만들어질 때 필수 값들이 잘 들어왔는지(무결성) 검증하는 진짜 생성자입니다.
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

    // ==========================================
    // ⚙️ 비즈니스 로직(메서드) 영역
    // 객체 지향의 핵심! 데이터를 수정할 때는 반드시 정해진 메서드(관문)를 통해서만 수정하도록 합니다.
    // ==========================================

    public void updateProudestMoment(String proudestMoment) {
        this.proudestMoment = validateAndTrim(proudestMoment);
    }

    public void updateHabitToImprove(String habitToImprove) {
        this.habitToImprove = validateAndTrim(habitToImprove);
    }

    public void updateSelfFeedback(String selfFeedback) {
        this.selfFeedback = validateAndTrim(selfFeedback);
    }

    public void updateNextResolution(String nextResolution) {
        this.nextResolution = validateAndTrim(nextResolution);
    }

    public void updateLearningConcepts(String knownConcepts, String unknownConcepts) {
        // ★ 아는 개념 / 모르는 개념에도 비속어 필터링이 적용되도록 보완했습니다!
        this.knownConcepts = validateAndTrim(knownConcepts);
        this.unknownConcepts = validateAndTrim(unknownConcepts);
    }

    public void updateMoodAndScore(String monthlyMood, Integer selfEffortScore) {
        if (selfEffortScore != null && (selfEffortScore < 1 || selfEffortScore > 5)) {
            throw new IllegalArgumentException("노력 만족도(별점)는 1점에서 5점 사이여야 합니다.");
        }
        // ★ 학생들이 "존나 우울해" 처럼 기분에도 비속어를 쓸 수 있으므로 필터링을 추가했습니다!
        this.monthlyMood = validateAndTrim(monthlyMood);
        this.selfEffortScore = selfEffortScore;
    }

    public void setPastResolution(String pastResolution) {
        this.pastResolution = validateAndTrim(pastResolution);
    }

    /**
     * (공통 내부 헬퍼 메소드) 모든 문자열 데이터가 저장되기 전에 무조건 거쳐가는 검문소입니다.
     * 여백 제거, 길이 검사, 비속어 필터링(***)을 수행합니다.
     */
    private String validateAndTrim(String input) {
        // 1. null(빈칸) 처리
        if (input == null) {
            return "";
        }

        // 2. 앞뒤 쓸데없는 띄어쓰기(여백) 제거
        String sanitized = input.trim();

        // 3. 길이 제한 방어 (해커들의 테러 방지)
        if (sanitized.length() > 1000) {
            throw new IllegalArgumentException("입력 내용이 1000자를 초과할 수 없습니다.");
        }

        // 4. 비속어 필터링 (VaneProject 라이브러리 사용)
        // 발견된 비속어를 자동으로 '***' 처리하여 돌려줍니다.
        sanitized = badWordFiltering.change(sanitized);

        return sanitized;
    }

    // ==========================================
    // 📤 Getter 영역 (데이터 꺼내 쓰기)
    // ==========================================
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
    public Integer getBooksReadCount() { return booksReadCount; }
    public Double getExamCompletionRate() { return examCompletionRate; }
    public Double getRetestCompletionRate() { return retestCompletionRate; }
    public Integer getStudyAttendanceDays() { return studyAttendanceDays; }
    public Integer getOvercomeWrongCount() { return overcomeWrongCount; }

    // ==========================================
    // 📥 Setter 영역 (프레임워크나 외부에서 직접 값을 꽂아넣을 때 사용)
    // 모든 Setter가 반드시 validateAndTrim(검문소)을 거치도록 통일성을 맞췄습니다!
    // ==========================================
    public void setProudestMoment(String proudestMoment) {
        this.proudestMoment = validateAndTrim(proudestMoment);
    }

    public void setHabitToImprove(String habitToImprove) {
        this.habitToImprove = validateAndTrim(habitToImprove);
    }

    public void setSelfFeedback(String selfFeedback) {
        this.selfFeedback = validateAndTrim(selfFeedback);
    }

    public void setNextResolution(String nextResolution) {
        this.nextResolution = validateAndTrim(nextResolution);
    }

    public void setKnownConcepts(String knownConcepts) {
        this.knownConcepts = validateAndTrim(knownConcepts);
    }

    public void setUnknownConcepts(String unknownConcepts) {
        this.unknownConcepts = validateAndTrim(unknownConcepts);
    }

    public void setMonthlyMood(String monthlyMood) {
        this.monthlyMood = validateAndTrim(monthlyMood);
    }
}