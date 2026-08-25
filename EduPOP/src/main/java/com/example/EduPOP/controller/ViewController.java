package com.example.EduPOP.controller;

import com.example.EduPOP.domain.report.StudentReport;
import com.example.EduPOP.service.StudentReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    // 1. 화면 띄울 때도 주방장(Service)의 도움이 필요합니다.
    private final StudentReportService studentReportService;

    public ViewController(StudentReportService studentReportService) {
        this.studentReportService = studentReportService;
    }

    @GetMapping("/test")
    public String showTestPage(Model model) {
        // 1. 이번 달 리포트 가져오기 (DB에 밀어넣은 2번)
        StudentReport currentReport = studentReportService.getReport(2L);

        // 2. 지난 달 리포트 가져오기 (DB에 밀어넣은 1번)
        StudentReport lastMonthReport = studentReportService.getReport(1L);

        // 3. 🚀 두 리포트의 행동 지표를 비교해서 화면(Model)에 넘겨주기!
        if (lastMonthReport != null && currentReport != null) {

            // Null 방어 로직: DB에 값이 비어있으면 0으로 계산합니다.
            int currBooks = currentReport.getBooksReadCount() != null ? currentReport.getBooksReadCount() : 0;
            int lastBooks = lastMonthReport.getBooksReadCount() != null ? lastMonthReport.getBooksReadCount() : 0;
            model.addAttribute("lastMonthBooks", lastBooks);
            model.addAttribute("booksDiff", currBooks - lastBooks);

            int currExam = currentReport.getExamCompletionRate() != null ? currentReport.getExamCompletionRate().intValue() : 0;
            int lastExam = lastMonthReport.getExamCompletionRate() != null ? lastMonthReport.getExamCompletionRate().intValue() : 0;
            model.addAttribute("lastMonthExam", lastExam);
            model.addAttribute("examDiff", currExam - lastExam);

            int currRetest = currentReport.getRetestCompletionRate() != null ? currentReport.getRetestCompletionRate().intValue() : 0;
            int lastRetest = lastMonthReport.getRetestCompletionRate() != null ? lastMonthReport.getRetestCompletionRate().intValue() : 0;
            model.addAttribute("lastMonthRetest", lastRetest);
            model.addAttribute("retestDiff", currRetest - lastRetest);

            int currAtt = currentReport.getStudyAttendanceDays() != null ? currentReport.getStudyAttendanceDays() : 0;
            int lastAtt = lastMonthReport.getStudyAttendanceDays() != null ? lastMonthReport.getStudyAttendanceDays() : 0;
            model.addAttribute("lastMonthAtt", lastAtt);
            model.addAttribute("attDiff", currAtt - lastAtt);

            int currOvercome = currentReport.getOvercomeWrongCount() != null ? currentReport.getOvercomeWrongCount() : 0;
            int lastOvercome = lastMonthReport.getOvercomeWrongCount() != null ? lastMonthReport.getOvercomeWrongCount() : 0;
            model.addAttribute("lastMonthOvercome", lastOvercome);
            model.addAttribute("overcomeDiff", currOvercome - lastOvercome);
        }

        // 4. ★ currentReport 라는 이름으로 꺼냈으니, 넘길 때도 currentReport 를 넘깁니다!
        model.addAttribute("report", currentReport);

        return "test";
    }
}