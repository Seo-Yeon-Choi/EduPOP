package com.example.EduPOP.controller;

import com.example.EduPOP.domain.report.StudentReport;
import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.domain.user.UserRole;
import com.example.EduPOP.service.report.StudentReportService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

@Controller
public class ViewController {

    private final StudentReportService studentReportService;

    public ViewController(
            StudentReportService studentReportService
    ) {
        this.studentReportService = studentReportService;
    }

    // =========================================================
    // 학생 "나의 리포트" 화면
    //
    // 학생이 로그인한 후 "나의 리포트"를 클릭하면
    // 현재 로그인한 학생의 userId를 이용하여
    // 해당 학생의 리포트를 자동으로 조회한다.
    //
    // reportId를 1L, 2L처럼 하드코딩하지 않는다.
    // =========================================================

    @GetMapping("/student/report")
    public String showStudentReport(
            HttpSession session,
            Model model
    ) {

        // -----------------------------------------------------
        // 현재 로그인한 사용자 가져오기
        // -----------------------------------------------------

        User loginUser =
                (User) session.getAttribute("loginUser");

        // 로그인하지 않았다면 로그인 페이지로 이동
        if (loginUser == null) {
            return "redirect:/LocalLogin";
        }

        // 학생만 접근 가능
        if (loginUser.getRole() != UserRole.STUDENT) {
            return "redirect:/";
        }

        // 현재 로그인한 학생의 user_id
        Long studentId = loginUser.getUserId();


        // =====================================================
        // 1. 이번 달 학생 리포트 생성 또는 최신화
        //
        // 기존 리포트가 있어도 다시 집계해야 시험 응시율과
        // 극복 문제 수가 학부모 리포트와 같은 최신 값이 된다.
        // =====================================================

        LocalDate today = LocalDate.now();

        LocalDate periodStart =
                today.withDayOfMonth(1); // 이번 달 첫날

        LocalDate periodEnd =
                today.with(TemporalAdjusters.lastDayOfMonth()); // 이번 달 마지막 날

        StudentReport currentReport =
                studentReportService.createMonthlyReport(
                        studentId,
                        periodStart,
                        periodEnd
                );


        // =====================================================
        // 3. 현재 리포트를 화면에 전달
        // =====================================================

        model.addAttribute(
                "report",
                currentReport
        );


        // =====================================================
        // 4. 이전 리포트 조회
        // =====================================================

        StudentReport lastMonthReport =
                studentReportService
                        .getPreviousReport(
                                studentId,
                                currentReport.getPeriodStart()
                        );


        // =====================================================
        // 5. 지난 달과 비교할 데이터
        // =====================================================

        if (lastMonthReport != null) {

            // -------------------------------------------------
            // 읽은 책
            // -------------------------------------------------

            int currentBooks =
                    currentReport.getBooksReadCount() != null
                            ? currentReport.getBooksReadCount()
                            : 0;

            int previousBooks =
                    lastMonthReport.getBooksReadCount() != null
                            ? lastMonthReport.getBooksReadCount()
                            : 0;

            model.addAttribute(
                    "lastMonthBooks",
                    previousBooks
            );

            model.addAttribute(
                    "booksDiff",
                    currentBooks - previousBooks
            );


            // -------------------------------------------------
            // 시험 응시율
            // -------------------------------------------------

            int currentExam =
                    currentReport.getExamCompletionRate() != null
                            ? currentReport
                            .getExamCompletionRate()
                            .intValue()
                            : 0;

            int previousExam =
                    lastMonthReport.getExamCompletionRate() != null
                            ? lastMonthReport
                            .getExamCompletionRate()
                            .intValue()
                            : 0;

            model.addAttribute(
                    "lastMonthExam",
                    previousExam
            );

            model.addAttribute(
                    "examDiff",
                    currentExam - previousExam
            );


            // -------------------------------------------------
            // 재시험 응시율
            // -------------------------------------------------

            int currentRetest =
                    currentReport.getRetestCompletionRate() != null
                            ? currentReport
                            .getRetestCompletionRate()
                            .intValue()
                            : 0;

            int previousRetest =
                    lastMonthReport
                            .getRetestCompletionRate() != null
                            ? lastMonthReport
                            .getRetestCompletionRate()
                            .intValue()
                            : 0;

            model.addAttribute(
                    "lastMonthRetest",
                    previousRetest
            );

            model.addAttribute(
                    "retestDiff",
                    currentRetest - previousRetest
            );


            // -------------------------------------------------
            // 학습 출석 일수
            // -------------------------------------------------

            int currentAttendance =
                    currentReport.getStudyAttendanceDays() != null
                            ? currentReport
                            .getStudyAttendanceDays()
                            : 0;

            int previousAttendance =
                    lastMonthReport
                            .getStudyAttendanceDays() != null
                            ? lastMonthReport
                            .getStudyAttendanceDays()
                            : 0;

            model.addAttribute(
                    "lastMonthAtt",
                    previousAttendance
            );

            model.addAttribute(
                    "attDiff",
                    currentAttendance - previousAttendance
            );


            // -------------------------------------------------
            // 극복한 문제 수
            // -------------------------------------------------

            int currentOvercome =
                    currentReport.getOvercomeWrongCount() != null
                            ? currentReport
                            .getOvercomeWrongCount()
                            : 0;

            int previousOvercome =
                    lastMonthReport
                            .getOvercomeWrongCount() != null
                            ? lastMonthReport
                            .getOvercomeWrongCount()
                            : 0;

            model.addAttribute(
                    "lastMonthOvercome",
                    previousOvercome
            );

            model.addAttribute(
                    "overcomeDiff",
                    currentOvercome - previousOvercome
            );

        } else {

            // -------------------------------------------------
            // 이전 달 리포트가 없는 첫 리포트
            // -------------------------------------------------

            model.addAttribute(
                    "lastMonthBooks",
                    0
            );

            model.addAttribute(
                    "booksDiff",
                    0
            );

            model.addAttribute(
                    "lastMonthExam",
                    0
            );

            model.addAttribute(
                    "examDiff",
                    0
            );

            model.addAttribute(
                    "lastMonthRetest",
                    0
            );

            model.addAttribute(
                    "retestDiff",
                    0
            );

            model.addAttribute(
                    "lastMonthAtt",
                    0
            );

            model.addAttribute(
                    "attDiff",
                    0
            );

            model.addAttribute(
                    "lastMonthOvercome",
                    0
            );

            model.addAttribute(
                    "overcomeDiff",
                    0
            );
        }

        return "test";
    }
}
