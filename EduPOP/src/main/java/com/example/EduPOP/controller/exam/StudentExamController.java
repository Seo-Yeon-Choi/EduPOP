package com.example.EduPOP.controller.exam;

import com.example.EduPOP.domain.common.Paging;
import com.example.EduPOP.domain.exam.*;
import com.example.EduPOP.service.exam.StudentExamService;
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

    // TODO 로그인 기능 구현 후 세션의 studentId로 변경
    private static final Long TEMP_STUDENT_ID = 9002L;

    @GetMapping
    public String examList(
            @RequestParam(defaultValue = "1") int page,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,

            Model model
    ) {

        Paging paging =
                studentExamService.getStudentExamPaging(
                        TEMP_STUDENT_ID,
                        date,
                        page
                );

        List<StudentExam> exams =
                studentExamService.getStudentExams(
                        TEMP_STUDENT_ID,
                        date,
                        paging
                );

        int todayReviewCount =
                studentExamService.getTodayReviewCount(
                        TEMP_STUDENT_ID
                );

        model.addAttribute("exams", exams);

        model.addAttribute("todayReviewCount", todayReviewCount);

        model.addAttribute("paging", paging);

        model.addAttribute("selectedDate", date);

        System.out.println("선택 날짜 = " + date);

        return "student/exam/list";
    }

    @GetMapping("/{examId}/take")
    public String takeExam(@PathVariable Long examId, Model model) {

        Exam exam = studentExamService.getExam(examId);

        if (exam.getExamType()!= ExamType.WORD) {
            throw new IllegalArgumentException("단어 시험만 응시할 수 있습니다.");
        }

        ExamAttempt attempt = studentExamService.startWordExam(examId, TEMP_STUDENT_ID);

        List<ExamQuestion> questions = studentExamService.getWordExamQuestions(examId);

        model.addAttribute("exam", exam);
        model.addAttribute("attempt", attempt);
        model.addAttribute("questions", questions);
        model.addAttribute("mode", "EXAM");

        return "student/exam/take";
    }

    @GetMapping("/{examId}/review")
    public String reviewExam(@PathVariable Long examId, Model model) {

        Exam exam = studentExamService.getExam(examId);

        if (exam.getExamType()==ExamType.WORD) {
            throw new IllegalArgumentException("단어 시험은 복습 방식이 아닙니다.");
        }

        ExamAttempt attempt = studentExamService.startReview(examId, TEMP_STUDENT_ID);

        List<ExamQuestion> questions = studentExamService.getReviewQuestions(examId, TEMP_STUDENT_ID);

        model.addAttribute("exam", exam);
        model.addAttribute("attempt", attempt);
        model.addAttribute("questions", questions);
        model.addAttribute("mode", "REVIEW");

        return "student/exam/take";
    }

    @PostMapping("/submit")
    @ResponseBody
    public Map<String, Object> submit(@RequestBody ExamSubmission submission) {

        Long attemptId = studentExamService.submitExam(TEMP_STUDENT_ID, submission);

        return Map.of("success", true, "attemptId", attemptId);
    }

    @GetMapping("/attempts/{attemptId}/result")
    public String result(@PathVariable Long attemptId, @RequestParam(defaultValue = "submit") String from, Model model) {

        ExamAttempt attempt = studentExamService.getResult(attemptId, TEMP_STUDENT_ID);

        Exam exam = studentExamService.getExam(attempt.getExamId());

        List<ExamAnswer> answers = studentExamService.getResultAnswers(attemptId);

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
            Model model) {

        Paging paging =
                studentExamService
                        .getStudentResultPaging(
                                TEMP_STUDENT_ID,
                                date,
                                page
                        );

        List<StudentExamResult> results =
                studentExamService
                        .getStudentExamResults(
                                TEMP_STUDENT_ID,
                                date,
                                paging
                        );

        model.addAttribute("results", results);

        model.addAttribute("paging", paging);

        model.addAttribute("selectedDate", date);

        return "layout/exam/result-list";
    }

    @GetMapping("/today-review")
    public String todayReview(Model model) {

        DailyReviewAttempt attempt = studentExamService.startTodayReview(TEMP_STUDENT_ID);

        List<ExamQuestion> questions = studentExamService.getTodayReviewQuestions(TEMP_STUDENT_ID);

        model.addAttribute("attempt", attempt);

        model.addAttribute("questions", questions);

        model.addAttribute("mode", "DAILY_REVIEW");

        return "student/exam/today-review";
    }

    @PostMapping("/today-review/submit")
    @ResponseBody
    public Map<String, Object> submitTodayReview(@RequestBody DailyReviewSubmission submission) {

        Long attemptId = studentExamService.submitTodayReview(TEMP_STUDENT_ID, submission);

        return Map.of("success", true, "attemptId", attemptId);
    }

    @GetMapping("/today-review/{dailyReviewAttemptId}/result")
    public String todayReviewResult(
            @PathVariable Long dailyReviewAttemptId,
            @RequestParam(defaultValue = "submit")
            String from,
            Model model) {

        DailyReviewAttempt attempt =
                studentExamService
                        .getDailyReviewResult(
                                dailyReviewAttemptId,
                                TEMP_STUDENT_ID
                        );

        List<DailyReviewAnswer> answers =
                studentExamService
                        .getDailyReviewResultAnswers(
                                dailyReviewAttemptId
                        );

        model.addAttribute(
                "attempt",
                attempt
        );

        model.addAttribute(
                "answers",
                answers
        );

        model.addAttribute(
                "from",
                from
        );

        return "student/exam/today-review-result";
    }
}