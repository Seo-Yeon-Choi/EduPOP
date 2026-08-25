package com.example.EduPOP.controller.exam;

import com.example.EduPOP.domain.common.Paging;
import com.example.EduPOP.domain.exam.*;
import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.service.exam.StudentExamService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/student/exams")
public class StudentExamController {

    private final StudentExamService studentExamService;

    @GetMapping
    public String examList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            HttpSession session,
            Model model
    ) {

        Long studentId = getLoginStudentId(session);

        if (studentId == null) {
            return "redirect:/LocalLogin";
        }

        Paging paging =
                studentExamService.getStudentExamPaging(
                        studentId,
                        date,
                        page
                );

        List<StudentExam> exams =
                studentExamService.getStudentExams(
                        studentId,
                        date,
                        paging
                );

        int todayReviewCount =
                studentExamService.getTodayReviewCount(
                        studentId
                );

        model.addAttribute("exams", exams);
        model.addAttribute("todayReviewCount", todayReviewCount);
        model.addAttribute("paging", paging);
        model.addAttribute("selectedDate", date);

        return "student/exam/list";
    }

    @GetMapping("/{examId}/take")
    public String takeExam(
            @PathVariable Long examId,
            HttpSession session,
            Model model
    ) {

        Long studentId = getLoginStudentId(session);

        if (studentId == null) {
            return "redirect:/LocalLogin";
        }

        Exam exam = studentExamService.getExam(examId);

        if (exam.getExamType() != ExamType.WORD) {
            throw new IllegalArgumentException(
                    "단어 시험만 응시할 수 있습니다."
            );
        }

        ExamAttempt attempt =
                studentExamService.startWordExam(
                        examId,
                        studentId
                );

        List<ExamQuestion> questions =
                studentExamService.getWordExamQuestions(
                        examId
                );

        model.addAttribute("exam", exam);
        model.addAttribute("attempt", attempt);
        model.addAttribute("questions", questions);
        model.addAttribute("mode", "EXAM");

        return "student/exam/take";
    }

    @GetMapping("/{examId}/review")
    public String reviewExam(
            @PathVariable Long examId,
            HttpSession session,
            Model model
    ) {

        Long studentId = getLoginStudentId(session);

        if (studentId == null) {
            return "redirect:/LocalLogin";
        }

        Exam exam =
                studentExamService.getExam(
                        examId
                );

        if (exam.getExamType() == ExamType.WORD) {
            throw new IllegalArgumentException(
                    "단어 시험은 복습 방식이 아닙니다."
            );
        }

        ExamAttempt attempt =
                studentExamService.startReview(
                        examId,
                        studentId
                );

        List<ExamQuestion> questions =
                studentExamService.getReviewQuestions(
                        examId,
                        studentId
                );

        model.addAttribute("exam", exam);
        model.addAttribute("attempt", attempt);
        model.addAttribute("questions", questions);
        model.addAttribute("mode", "REVIEW");

        return "student/exam/take";
    }

    @PostMapping("/submit")
    @ResponseBody
    public Map<String, Object> submit(
            @RequestBody ExamSubmission submission,
            HttpSession session
    ) {

        Long studentId = getLoginStudentId(session);

        if (studentId == null) {
            return Map.of(
                    "success", false,
                    "message", "로그인이 필요합니다."
            );
        }

        Long attemptId =
                studentExamService.submitExam(
                        studentId,
                        submission
                );

        return Map.of(
                "success", true,
                "attemptId", attemptId
        );
    }

    @GetMapping("/attempts/{attemptId}/result")
    public String result(
            @PathVariable Long attemptId,
            @RequestParam(defaultValue = "submit") String from,
            HttpSession session,
            Model model
    ) {

        Long studentId = getLoginStudentId(session);

        if (studentId == null) {
            return "redirect:/LocalLogin";
        }

        ExamAttempt attempt =
                studentExamService.getResult(
                        attemptId,
                        studentId
                );

        Exam exam =
                studentExamService.getExam(
                        attempt.getExamId()
                );

        List<ExamAnswer> answers =
                studentExamService.getResultAnswers(
                        attemptId
                );

        model.addAttribute("exam", exam);
        model.addAttribute("attempt", attempt);
        model.addAttribute("answers", answers);
        model.addAttribute("from", from);

        return "student/exam/result";
    }

    @GetMapping("/results")
    public String resultList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            HttpSession session,
            Model model
    ) {

        Long studentId = getLoginStudentId(session);

        if (studentId == null) {
            return "redirect:/LocalLogin";
        }

        Paging paging =
                studentExamService.getStudentResultPaging(
                        studentId,
                        date,
                        page
                );

        List<StudentExamResult> results =
                studentExamService.getStudentExamResults(
                        studentId,
                        date,
                        paging
                );

        model.addAttribute("results", results);
        model.addAttribute("paging", paging);
        model.addAttribute("selectedDate", date);

        return "layout/exam/result-list";
    }

    @GetMapping("/today-review")
    public String todayReview(
            HttpSession session,
            Model model
    ) {

        Long studentId = getLoginStudentId(session);

        if (studentId == null) {
            return "redirect:/LocalLogin";
        }

        DailyReviewAttempt attempt =
                studentExamService.startTodayReview(
                        studentId
                );

        List<ExamQuestion> questions =
                studentExamService.getTodayReviewQuestions(
                        studentId
                );

        model.addAttribute("attempt", attempt);
        model.addAttribute("questions", questions);
        model.addAttribute("mode", "DAILY_REVIEW");

        return "student/exam/today-review";
    }

    @PostMapping("/today-review/submit")
    @ResponseBody
    public Map<String, Object> submitTodayReview(
            @RequestBody DailyReviewSubmission submission,
            HttpSession session
    ) {

        Long studentId = getLoginStudentId(session);

        if (studentId == null) {
            return Map.of(
                    "success", false,
                    "message", "로그인이 필요합니다."
            );
        }

        Long attemptId =
                studentExamService.submitTodayReview(
                        studentId,
                        submission
                );

        return Map.of(
                "success", true,
                "attemptId", attemptId
        );
    }

    @GetMapping("/today-review/{dailyReviewAttemptId}/result")
    public String todayReviewResult(
            @PathVariable Long dailyReviewAttemptId,
            @RequestParam(defaultValue = "submit") String from,
            HttpSession session,
            Model model
    ) {

        Long studentId = getLoginStudentId(session);

        if (studentId == null) {
            return "redirect:/LocalLogin";
        }

        DailyReviewAttempt attempt =
                studentExamService.getDailyReviewResult(
                        dailyReviewAttemptId,
                        studentId
                );

        List<DailyReviewAnswer> answers =
                studentExamService.getDailyReviewResultAnswers(
                        dailyReviewAttemptId
                );

        model.addAttribute("attempt", attempt);
        model.addAttribute("answers", answers);
        model.addAttribute("from", from);

        return "student/exam/today-review-result";
    }

    private Long getLoginStudentId(
            HttpSession session
    ) {

        User loginUser =
                (User) session.getAttribute(
                        "loginUser"
                );

        if (loginUser == null) {
            return null;
        }

        return loginUser.getUserId();
    }
}