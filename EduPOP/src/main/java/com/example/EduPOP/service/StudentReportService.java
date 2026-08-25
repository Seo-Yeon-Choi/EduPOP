package com.example.EduPOP.service;

import com.example.EduPOP.domain.report.StudentReport;
// 곧 만들 창고(Repository)를 미리 불러옵니다! (지금은 빨간 줄이 뜨는 게 정상입니다)
import com.example.EduPOP.repository.StudentReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentReportService {

    // 주방장이 일할 때 사용할 창고(Repository)
    private final StudentReportRepository reportRepository;

    // 스프링 매니저가 창고를 주방장에게 쥐여줍니다 (의존성 주입)
    public StudentReportService(StudentReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    /**
     * 1. 칭찬하고 싶은 점 (Keep)
     */
    @Transactional
    public void updateProudestMoment(Long reportId, String proudestMoment) {
        StudentReport report = getReportOrThrow(reportId);
        report.updateProudestMoment(proudestMoment);
    }

    /**
     * 2. 고치고 싶은 습관 (Problem)
     */
    @Transactional
    public void updateHabitToImprove(Long reportId, String habitToImprove) {
        StudentReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("리포트를 찾을 수 없습니다: " + reportId));
        report.setHabitToImprove(habitToImprove);
    }

    /**
     * 3. 셀프 피드백 (Feedback)
     */
    @Transactional
    public void updateSelfFeedback(Long reportId, String selfFeedback) {
        StudentReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("리포트를 찾을 수 없습니다: " + reportId));
        report.setSelfFeedback(selfFeedback);
    }

    /**
     * 4. 다음 달 다짐 (Try)
     */
    @Transactional
    public void updateNextResolution(Long reportId, String nextResolution) {
        StudentReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("리포트를 찾을 수 없습니다: " + reportId));
        report.setNextResolution(nextResolution);
    }

    /**
     * 5. 아는 개념 / 모르는 개념
     */
    @Transactional
    public void updateLearningConcepts(Long reportId, String knownConcepts, String unknownConcepts) {
        StudentReport report = getReportOrThrow(reportId);
        report.updateLearningConcepts(knownConcepts, unknownConcepts);
    }

    /**
     * 6. 별점 및 기분
     */
    @Transactional
    public void updateMoodAndScore(Long reportId, String monthlyMood, Integer selfEffortScore) {
        StudentReport report = getReportOrThrow(reportId);
        report.updateMoodAndScore(monthlyMood, selfEffortScore);
    }


    // ==========================================
    // (공통 헬퍼 메서드) 창고에서 리포트를 꺼내오고, 없으면 에러를 던집니다.
    // ==========================================
    private StudentReport getReportOrThrow(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 리포트를 찾을 수 없습니다: " + reportId));
    }
    // ==========================================
    // 새로 추가할 요리법: DB에 빈 리포트 생성하기
    // ==========================================
    @Transactional
    // ==========================================
    // 화면에 띄워줄 리포트 데이터를 DB에서 통째로 꺼내오기!
    // ==========================================
    public StudentReport getReport(Long reportId) {
        return reportRepository.findById(reportId).orElse(null);
    }
}