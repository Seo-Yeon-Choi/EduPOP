package com.example.EduPOP.domain.report;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "parent_reports")
public class ParentReport {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @Column(nullable = false) private Long studentId;
    @Column(nullable = false) private Long createdBy; // 리포트를 작성한 교사 ID
    @Column(nullable = false) private LocalDate periodStart; // 평가 시작일
    @Column(nullable = false) private LocalDate periodEnd;   // 평가 종료일

    // 외부 공유용 고유 링크 토큰 (자동 생성)
    @Column(nullable = false, unique = true, length = 100)
    private String accessToken = UUID.randomUUID().toString();

    private Double monthlyExamScore = 0.0;
    private Double classAverageScore = 0.0;
    @Column(length = 50) private String comprehensionLevel = "F";
    private Double wordExamCompletionRate = 0.0;
    @Column(nullable = false) private Integer booksReadCount = 0;
    @Column(nullable = false) private Integer overcomeWrongCount = 0;
    @Column(length = 50) private String topWeakTypeTag = "";

    // 동적 영역별 성취도를 저장하기 위한 JSON 형태의 텍스트 컬럼
    @Column(columnDefinition = "TEXT")
    private String radarChartData = "{}";

    @Column(columnDefinition = "TEXT") private String teacherComment = "";
    @Column(nullable = false, length = 20) private String status = "DRAFT";
    @Column(nullable = false) private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime publishedAt;

    // DB 테이블에는 없지만 화면 출력을 위해 임시로 담아두는 필드 (@Transient)
    @Transient private String studentName;
    @Transient private String gradeInfo;
    @Transient private String teacherName;

    protected ParentReport() {}

    public ParentReport(Long studentId, Long createdBy, LocalDate periodStart, LocalDate periodEnd) {
        this.studentId = studentId;
        this.createdBy = createdBy != null ? createdBy : 1L;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
    }

    // 학생의 월간 시험 점수를 기반으로 이해도 등급(A~F)을 자동 계산
    public void calculateComprehensionLevel() {
        if (this.monthlyExamScore == null) { this.comprehensionLevel = "-"; return; }
        if (this.monthlyExamScore >= 90) this.comprehensionLevel = "A";
        else if (this.monthlyExamScore >= 80) this.comprehensionLevel = "B";
        else if (this.monthlyExamScore >= 70) this.comprehensionLevel = "C";
        else if (this.monthlyExamScore >= 60) this.comprehensionLevel = "D";
        else this.comprehensionLevel = "F";
    }

    // 성적 관련 데이터를 한 번에 세팅 (발행 시점 스냅샷 용도)
    public void setSnapshotData(Double monthly, Double avg, String radarJson, String fetchedComment) {
        this.monthlyExamScore = monthly;
        this.classAverageScore = avg;
        this.radarChartData = radarJson != null ? radarJson : "{}";
        this.teacherComment = fetchedComment != null ? fetchedComment : "아직 등록된 코멘트가 없습니다.";
        calculateComprehensionLevel();
    }

    // 학습 태도 관련 데이터를 한 번에 세팅
    public void setAttitude(Double wordRate, Integer books, Integer overcome, String weakType) {
        this.wordExamCompletionRate = wordRate;
        this.booksReadCount = books != null ? books : 0;
        this.overcomeWrongCount = overcome != null ? overcome : 0;
        this.topWeakTypeTag = weakType;
    }

    public void setStudentInfo(String name, String grade) {
        this.studentName = name;
        this.gradeInfo = grade;
    }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    // === Getters ===
    public Long getReportId() { return reportId; }
    public Long getStudentId() { return studentId; }
    public Long getCreatedBy() { return createdBy; }
    public String getStudentName() { return studentName; }
    public String getGradeInfo() { return gradeInfo; }
    public LocalDate getPeriodStart() { return periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public Double getMonthlyExamScore() { return monthlyExamScore; }
    public Double getClassAverageScore() { return classAverageScore; }
    public String getComprehensionLevel() { return comprehensionLevel; }
    public Double getWordExamCompletionRate() { return wordExamCompletionRate; }
    public Integer getBooksReadCount() { return booksReadCount; }
    public Integer getOvercomeWrongCount() { return overcomeWrongCount; }
    public String getTopWeakTypeTag() { return topWeakTypeTag; }
    public String getTeacherComment() { return teacherComment; }
    public String getTeacherName() { return teacherName; }
    public String getRadarChartData() { return radarChartData; }
    // ========================================================
    // 🚀 추가 부품 1: 토큰 가져오기 (이게 없어서 getAccessToken 에러가 났습니다!)
    // ========================================================
    public String getAccessToken() {
        return accessToken;
    }

    // ========================================================
    // 🚀 추가 부품 2: 리포트 발송 상태 변경 (이게 없어서 publish 에러가 났습니다!)
    // ========================================================
    public void publish() {
        if ("DRAFT".equals(this.status)) {
            this.status = "PUBLISHED";
            this.publishedAt = LocalDateTime.now();
        }
    }
}