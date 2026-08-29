package com.example.EduPOP.service.report;

import com.example.EduPOP.domain.report.StudentReport;
import com.example.EduPOP.repository.StudentReportRepository;
import com.example.EduPOP.repository.exam.StudentExamMapper;
import com.example.EduPOP.repository.reading.ReadingMapper;
import com.example.EduPOP.repository.report.ReportMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class StudentReportService {

    // =========================================================
    // student_reports 테이블 저장/조회
    // =========================================================

    private final StudentReportRepository reportRepository;

    // =========================================================
    // 다른 팀원의 기능에서 데이터를 가져오기 위한 Mapper
    // =========================================================

    private final ReadingMapper readingMapper;
    private final StudentExamMapper studentExamMapper;
    private final ReportMapper reportMapper;

    public StudentReportService(
            StudentReportRepository reportRepository,
            ReadingMapper readingMapper,
            StudentExamMapper studentExamMapper,
            ReportMapper reportMapper
    ) {

        this.reportRepository = reportRepository;
        this.readingMapper = readingMapper;
        this.studentExamMapper = studentExamMapper;
        this.reportMapper = reportMapper;
    }


    // =========================================================
    // 월간 리포트 생성
    //
    // 학생이 직접 입력하는 회고값은 건드리지 않는다.
    //
    // 다른 팀원의 기능에서 다음 데이터를 가져와
    // student_reports에 자동 저장한다.
    //
    // booksReadCount
    // examCompletionRate
    // retestCompletionRate
    // studyAttendanceDays
    // overcomeWrongCount
    //
    // 또한 이전 리포트의 nextResolution을
    // 현재 리포트의 pastResolution으로 자동 연결한다.
    // =========================================================

    @Transactional
    public StudentReport createMonthlyReport(
            Long studentId,
            LocalDate periodStart,
            LocalDate periodEnd
    ) {

        validatePeriod(
                studentId,
                periodStart,
                periodEnd
        );

        // -----------------------------------------------------
        // 동일 학생 + 동일 기간 리포트가 이미 존재한다면
        // 기존 리포트를 사용한다.
        // -----------------------------------------------------

        StudentReport report =
                reportRepository
                        .findByStudentIdAndPeriodStartAndPeriodEnd(
                                studentId,
                                periodStart,
                                periodEnd
                        )
                        .orElseGet(() ->
                                new StudentReport(
                                        studentId,
                                        periodStart,
                                        periodEnd
                                )
                        );


        // =====================================================
        // 1. 독서
        //
        // reading_reports 테이블에서
        // 해당 기간에 작성한 독서감상문 수를 가져온다.
        // =====================================================

        int booksReadCount =
                readingMapper
                        .countReadingReportsByStudentIdAndPeriod(
                                studentId,
                                periodStart,
                                periodEnd
                        );


        // =====================================================
        // 2. 시험 응시율
        //
        // StudentExamMapper에서 계산한다.
        // =====================================================

        Double examCompletionRate =
                studentExamMapper.findExamCompletionRate(
                        studentId,
                        periodStart,
                        periodEnd
                );


        // =====================================================
        // 3. 재시험 응시율
        //
        // 네가 지정한 기준:
        //
        // exam_attempts.attempt_no >= 2
        //
        // 를 재시험으로 계산한다.
        // =====================================================

        Double retestCompletionRate =
                studentExamMapper.findRetestCompletionRate(
                        studentId,
                        periodStart,
                        periodEnd
                );


        // =====================================================
        // 4. 학습 참여 일수
        //
        // 기존 팀원이 만들어 놓은 메서드를 그대로 이용한다.
        //
        // 시험 GRADED 날짜
        // +
        // daily_review GRADED 날짜
        //
        // 를 합쳐 중복 날짜를 제거한 결과다.
        // =====================================================

        List<LocalDate> studyDates =
                studentExamMapper.findStudentGrowthStudyDates(
                        studentId,
                        periodStart,
                        periodEnd
                );

        int studyAttendanceDays =
                studyDates != null
                        ? studyDates.size()
                        : 0;


        // =====================================================
        // 5. 극복한 오답 문제 수
        //
        // 최초 시험에서 틀린 문제를 시험별 복습 또는 오늘의 복습에서
        // 다시 맞힌 고유 문항 수를 공통 ReportMapper로 가져온다.
        // =====================================================

        Integer overcomeWrongCount =
                reportMapper.countOvercomeWrongCount(
                        studentId,
                        periodStart,
                        periodEnd
                );


        // =====================================================
        // 6. 자동 집계 데이터 저장
        // =====================================================

        report.updateObjectiveMetrics(
                booksReadCount,
                examCompletionRate,
                retestCompletionRate,
                studyAttendanceDays,
                overcomeWrongCount
        );


        // =====================================================
        // 7. 이전 리포트 연결
        //
        // reportId - 1로 찾지 않는다.
        //
        // 현재 기간보다 이전에 끝난 리포트 중
        // 가장 최근 리포트를 찾는다.
        // =====================================================

        reportRepository
                .findTopByStudentIdAndPeriodEndBeforeOrderByPeriodEndDesc(
                        studentId,
                        periodStart
                )
                .ifPresent(previousReport ->

                        report.setPastResolution(
                                previousReport.getNextResolution()
                        )
                );


        // =====================================================
        // 8. 최종 저장
        // =====================================================

        return reportRepository.save(report);
    }


    // =========================================================
    // 학생의 최신 리포트 조회
    //
    // 학생이 /student/report로 들어왔을 때 사용한다.
    // =========================================================

    @Transactional(readOnly = true)
    public StudentReport getLatestReportByStudentId(
            Long studentId
    ) {

        if (studentId == null) {
            throw new IllegalArgumentException(
                    "학생 ID가 없습니다."
            );
        }

        return reportRepository
                .findTopByStudentIdOrderByPeriodEndDesc(
                        studentId
                )
                .orElse(null);
    }


    // =========================================================
    // 특정 리포트 조회
    // =========================================================

    @Transactional(readOnly = true)
    public StudentReport getReport(
            Long reportId
    ) {

        return reportRepository
                .findById(reportId)
                .orElse(null);
    }


    // =========================================================
    // Keep
    // =========================================================

    @Transactional
    public void updateProudestMoment(
            Long reportId,
            String proudestMoment
    ) {

        StudentReport report =
                getReportOrThrow(reportId);

        report.updateProudestMoment(
                proudestMoment
        );
    }


    // =========================================================
    // Problem
    // =========================================================

    @Transactional
    public void updateHabitToImprove(
            Long reportId,
            String habitToImprove
    ) {

        StudentReport report =
                getReportOrThrow(reportId);

        report.updateHabitToImprove(
                habitToImprove
        );
    }


    // =========================================================
    // Self Feedback
    // =========================================================

    @Transactional
    public void updateSelfFeedback(
            Long reportId,
            String selfFeedback
    ) {

        StudentReport report =
                getReportOrThrow(reportId);

        report.updateSelfFeedback(
                selfFeedback
        );
    }


    // =========================================================
    // Try
    // =========================================================

    @Transactional
    public void updateNextResolution(
            Long reportId,
            String nextResolution
    ) {

        StudentReport report =
                getReportOrThrow(reportId);

        report.updateNextResolution(
                nextResolution
        );
    }


    // =========================================================
    // 아는 개념 / 모르는 개념
    // =========================================================

    @Transactional
    public void updateLearningConcepts(
            Long reportId,
            String knownConcepts,
            String unknownConcepts
    ) {

        StudentReport report =
                getReportOrThrow(reportId);

        report.updateLearningConcepts(
                knownConcepts,
                unknownConcepts
        );
    }


    // =========================================================
    // 기분 / 노력 만족도
    // =========================================================

    @Transactional
    public void updateMoodAndScore(
            Long reportId,
            String monthlyMood,
            Integer selfEffortScore
    ) {

        StudentReport report =
                getReportOrThrow(reportId);

        report.updateMoodAndScore(
                monthlyMood,
                selfEffortScore
        );
    }


    // =========================================================
    // 공통 리포트 조회
    // =========================================================

    private StudentReport getReportOrThrow(
            Long reportId
    ) {

        return reportRepository
                .findById(reportId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "리포트를 찾을 수 없습니다: "
                                        + reportId
                        )
                );
    }


    // =========================================================
    // 기간 검증
    // =========================================================

    private void validatePeriod(
            Long studentId,
            LocalDate periodStart,
            LocalDate periodEnd
    ) {

        if (studentId == null) {

            throw new IllegalArgumentException(
                    "학생 ID는 필수입니다."
            );
        }

        if (periodStart == null ||
                periodEnd == null) {

            throw new IllegalArgumentException(
                    "리포트 기간은 필수입니다."
            );
        }

        if (periodStart.isAfter(periodEnd)) {

            throw new IllegalArgumentException(
                    "리포트 시작일이 종료일보다 늦을 수 없습니다."
            );
        }
    }
    // =========================================================
// 현재 리포트보다 이전 기간의 가장 최근 리포트 조회
//
// reportId 숫자를 이용하지 않고
// 실제 날짜를 기준으로 이전 리포트를 찾는다.
// =========================================================

    @Transactional(readOnly = true)
    public StudentReport getPreviousReport(
            Long studentId,
            java.time.LocalDate currentPeriodStart
    ) {

        return reportRepository
                .findTopByStudentIdAndPeriodEndBeforeOrderByPeriodEndDesc(
                        studentId,
                        currentPeriodStart
                )
                .orElse(null);
    }
}
