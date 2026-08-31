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

        if (!"WORD".equalsIgnoreCase(exam.getExamType())) {
            throw new IllegalArgumentException("단어 시험만 응시할 수 있습니다.");
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

        if ("WORD".equalsIgnoreCase(exam.getExamType())) {
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

        StudentGrowthSummary growthSummary =
                studentExamService.getStudentGrowthSummary(
                        studentId
                );

        model.addAttribute("results", results);
        model.addAttribute("growthSummary", growthSummary);
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

    @PostMapping("/word-game-take/check")
    @ResponseBody
    public Map<String, Object> checkWordGameAnswer(
            @RequestBody Map<String, Object> request,
            HttpSession session
    ) {

        Long studentId = getLoginStudentId(session);

        if (studentId == null) {
            return Map.of(
                    "success", false,
                    "message", "로그인이 필요합니다."
            );
        }

        Object attemptIdValue =
                request.get("attemptId");

        Object questionIdValue =
                request.get("questionId");

        Object studentAnswerValue =
                request.get("studentAnswer");

        if (attemptIdValue == null ||
                questionIdValue == null) {

            return Map.of(
                    "success", false,
                    "message", "잘못된 요청입니다."
            );
        }

        Long attemptId =
                Long.valueOf(
                        attemptIdValue.toString()
                );

        Long questionId =
                Long.valueOf(
                        questionIdValue.toString()
                );

        String studentAnswer =
                studentAnswerValue == null
                        ? ""
                        : studentAnswerValue
                        .toString()
                        .trim();

        boolean correct =
                studentExamService
                        .checkWordGameAnswer(
                                studentId,
                                attemptId,
                                questionId,
                                studentAnswer
                        );

        return Map.of(
                "success", true,
                "correct", correct
        );
    }

    // 단어 시험 게임 선택
    @GetMapping("/{examId}/word-game-take")
    public String wordGameSelect(
            @PathVariable Long examId,
            HttpSession session,
            Model model
    ) {

        Long studentId =
                getLoginStudentId(session);

        if (studentId == null) {
            return "redirect:/LocalLogin";
        }

        Exam exam =
                studentExamService.getExam(
                        examId
                );

        if (exam == null) {
            throw new IllegalArgumentException(
                    "시험이 존재하지 않습니다."
            );
        }

        if (!"WORD".equalsIgnoreCase(
                exam.getExamType()
        )) {
            throw new IllegalArgumentException(
                    "단어 시험만 게임형으로 응시할 수 있습니다."
            );
        }

        model.addAttribute(
                "exam",
                exam
        );

        return "student/exam/game/word-game-select";
    }

    // 게임 실행
    @GetMapping("/{examId}/word-game/{gameType}")
    public String wordGameTake(
            @PathVariable Long examId,
            @PathVariable String gameType,
            HttpSession session,
            Model model
    ) {

        Long studentId =
                getLoginStudentId(session);

        if (studentId == null) {
            return "redirect:/LocalLogin";
        }

        Exam exam =
                studentExamService.getExam(
                        examId
                );

        if (exam == null) {
            throw new IllegalArgumentException(
                    "시험이 존재하지 않습니다."
            );
        }

        if (!"WORD".equalsIgnoreCase(
                exam.getExamType()
        )) {
            throw new IllegalArgumentException(
                    "단어 시험만 게임형으로 응시할 수 있습니다."
            );
        }

        // 허용된 게임인지 먼저 검사
        String normalizedGameType =
                gameType.toLowerCase();

        if (!List.of(
                "falling",
                "matching",
                "defense",
                "maze"
        ).contains(normalizedGameType)) {

            throw new IllegalArgumentException(
                    "존재하지 않는 게임입니다."
            );
        }

        ExamAttempt attempt =
                studentExamService.startWordGameExam(
                        examId,
                        studentId
                );

        List<ExamQuestion> questions =
                studentExamService.getWordExamQuestions(
                        examId
                );

        model.addAttribute(
                "exam",
                exam
        );

        model.addAttribute(
                "attempt",
                attempt
        );

        model.addAttribute(
                "questions",
                questions
        );

        model.addAttribute(
                "gameType",
                normalizedGameType
        );

        return switch (normalizedGameType) {

            case "falling" ->
                    "student/exam/game/word-game-falling";

            case "matching" ->
                    "student/exam/game/word-matching";

            case "defense" ->
                    "student/exam/game/word-defense";

            case "maze" ->
                    "student/exam/game/word-maze";

            default ->
                    throw new IllegalArgumentException(
                            "존재하지 않는 게임입니다."
                    );
        };
    }

    @GetMapping("/word-game-take/round")
    @ResponseBody
    public Map<String, Object> getWordGameRound(
            @RequestParam Long attemptId,
            @RequestParam Long questionId,
            HttpSession session
    ) {

        Long studentId = getLoginStudentId(session);

        if (studentId == null) {
            return Map.of(
                    "success", false,
                    "message", "로그인이 필요합니다."
            );
        }

        List<String> options =
                studentExamService.getWordGameOptions(
                        studentId,
                        attemptId,
                        questionId
                );

        return Map.of(
                "success", true,
                "options", options
        );
    }

    // 단어 짝맞추기 게임
    @GetMapping("/word-game/matching/data")
    @ResponseBody
    public Map<String, Object> getWordMatchingData(
            @RequestParam Long attemptId,
            HttpSession session
    ) {

        Long studentId =
                getLoginStudentId(session);

        if (studentId == null) {

            return Map.of(
                    "success",
                    false,
                    "message",
                    "로그인이 필요합니다."
            );
        }

        List<Map<String, Object>> pairs =
                studentExamService
                        .getWordMatchingPairs(
                                studentId,
                                attemptId
                        );

        return Map.of(
                "success",
                true,
                "pairs",
                pairs
        );
    }



}