package com.example.EduPOP.service.exam;

import com.example.EduPOP.domain.common.Paging;
import com.example.EduPOP.domain.exam.*;
import com.example.EduPOP.repository.exam.StudentExamMapper;
import com.example.EduPOP.service.ai.AiReviewQuestionService;
import com.example.EduPOP.service.exp.ExpService;
// ExpService(이엑스피 서비스): 완료된 시험 결과를 경험치 기능에 전달
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentExamService {

    private final StudentExamMapper studentExamMapper;
    private final ExpService expService; // 시험과 복습 완료 결과를 경험치 처리 객체에 전달

    private final AiReviewQuestionService aiReviewQuestionService;

    private static final int PAGE_SIZE=10;

    /**
     * 페이징 관련 코드
     */

    public List<StudentExam> getStudentExams(
            Long studentId,
            LocalDate date,
            Paging paging
    ) {

        return studentExamMapper.findStudentExams(
                studentId,
                date,
                paging.getPageSize(),
                paging.getOffset()
        );
    }

    public Paging getStudentExamPaging(
            Long studentId,
            LocalDate date,
            int page
    ) {

        int totalCount =
                studentExamMapper.countStudentExams(
                        studentId,
                        date
                );

        return new Paging(
                page,
                PAGE_SIZE,
                totalCount
        );
    }

    public List<StudentExam> getStudentExams(
            Long studentId,
            LocalDate date,
            int page
    ) {

        int totalCount =
                studentExamMapper.countStudentExams(
                        studentId,
                        date
                );

        Paging paging =
                new Paging(
                        page,
                        PAGE_SIZE,
                        totalCount
                );

        return studentExamMapper.findStudentExams(
                studentId,
                date,
                PAGE_SIZE,
                paging.getOffset()
        );
    }

    public Exam getExam(Long examId) {
        return studentExamMapper.findExamById(examId);
    }

    @Transactional
    public List<ExamQuestion> getWordExamQuestions(Long examId) {

        List<ExamQuestion> questions =
                studentExamMapper.findAllQuestions(examId);

        loadChoices(questions);

        return questions;
    }

    @Transactional
    public List<ExamQuestion> getReviewQuestions(
            Long examId,
            Long studentId) {

        ExamAttempt originalAttempt =
                studentExamMapper.findLatestOriginalAttempt(
                        examId,
                        studentId
                );

        if (originalAttempt == null) {
            throw new IllegalStateException(
                    "복습할 시험 기록이 없습니다."
            );
        }

        List<ExamQuestion> questions =
                studentExamMapper.findWrongQuestions(
                        originalAttempt.getAttemptId()
                );

        loadChoices(questions);

        return questions;
    }

    private void loadChoices(List<ExamQuestion> questions) {

        for (ExamQuestion question : questions) {

            if ("MULTIPLE_CHOICE".equalsIgnoreCase(question.getQuestionType())) {

                question.setChoices(
                        studentExamMapper.findChoices(
                                question.getQuestionId()
                        )
                );
            }
        }
    }

    @Transactional
    public ExamAttempt startWordExam(
            Long examId,
            Long studentId) {

        Exam exam = studentExamMapper.findExamById(examId);

        if (!"WORD".equalsIgnoreCase(exam.getExamType())) {
            throw new IllegalArgumentException(
                    "단어 시험이 아닙니다."
            );
        }

        List<ExamQuestion> questions =
                studentExamMapper.findAllQuestions(examId);

        return createAttempt(
                examId,
                studentId,
                "EXAM",
                null,
                questions
        );
    }

    @Transactional
    public ExamAttempt startReview(
            Long examId,
            Long studentId) {

        ExamAttempt sourceAttempt =
                studentExamMapper.findLatestOriginalAttempt(
                        examId,
                        studentId
                );

        if (sourceAttempt == null) {
            throw new IllegalStateException(
                    "복습할 시험 기록이 없습니다."
            );
        }

        List<ExamQuestion> questions =
                studentExamMapper.findWrongQuestions(
                        sourceAttempt.getAttemptId()
                );

        if (questions.isEmpty()) {
            throw new IllegalStateException(
                    "복습할 오답이 없습니다."
            );
        }

        return createAttempt(
                examId,
                studentId,
                "REVIEW",
                sourceAttempt.getAttemptId(),
                questions
        );
    }

    private ExamAttempt createAttempt(
            Long examId,
            Long studentId,
            String attemptType,
            Long sourceAttemptId,
            List<ExamQuestion> questions) {

        ExamAttempt existing =
                studentExamMapper.findInProgressAttempt(
                        examId,
                        studentId,
                        attemptType
                );

        if (existing != null) {
            return existing;
        }

        BigDecimal maxScore =
                questions.stream()
                        .map(ExamQuestion::getScore)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        int nextAttemptNo =
                studentExamMapper.findNextAttemptNo(
                        examId,
                        studentId
                );

        ExamAttempt attempt = new ExamAttempt();

        attempt.setExamId(examId);
        attempt.setStudentId(studentId);
        attempt.setAttemptNo(nextAttemptNo);

        attempt.setAttemptType(attemptType);
        attempt.setSourceAttemptId(sourceAttemptId);

        // 학생이 웹 화면에서 응시/복습하는 경우
        attempt.setEntryMethod("ONLINE");

        attempt.setMaxScore(maxScore);
        attempt.setTotalQuestionCount(
                questions.size()
        );

        studentExamMapper.insertAttempt(attempt);

        return attempt;
    }

    @Transactional
    public Long submitExam(
            Long studentId,
            ExamSubmission submission) {

        ExamAttempt attempt =
                studentExamMapper.findAttemptById(
                        submission.getAttemptId()
                );

        if (attempt == null) {
            throw new IllegalArgumentException(
                    "응시 기록이 존재하지 않습니다."
            );
        }

        if (!attempt.getStudentId().equals(studentId)) {
            throw new IllegalStateException(
                    "본인의 시험만 제출할 수 있습니다."
            );
        }

        if (!"IN_PROGRESS".equals(attempt.getStatus())) {
            throw new IllegalStateException(
                    "이미 제출된 시험입니다."
            );
        }

        int correctCount = 0;

        BigDecimal totalScore =
                BigDecimal.ZERO;

        BigDecimal maxScore =
                BigDecimal.ZERO;

        for (ExamAnswer answer : submission.getAnswers()) {

            ExamQuestion question =
                    studentExamMapper.findQuestionById(
                            answer.getQuestionId()
                    );

            if (question == null) {
                throw new IllegalArgumentException(
                        "존재하지 않는 문제입니다."
                );
            }

            if (!question.getExamId()
                    .equals(attempt.getExamId())) {

                throw new IllegalStateException(
                        "해당 시험의 문제가 아닙니다."
                );
            }

            boolean correct =
                    checkSubmittedAnswer(
                            question,
                            answer.getStudentAnswer()
                    );

            BigDecimal earnedScore =
                    correct
                            ? question.getScore()
                            : BigDecimal.ZERO;

            answer.setAttemptId(
                    attempt.getAttemptId()
            );

            answer.setIsCorrect(correct);
            answer.setEarnedScore(earnedScore);

            studentExamMapper.insertAnswer(answer);

            maxScore =
                    maxScore.add(
                            question.getScore()
                    );

            totalScore =
                    totalScore.add(
                            earnedScore
                    );

            if (correct) {
                correctCount++;
            }
        }

        attempt.setTotalScore(totalScore);
        attempt.setMaxScore(maxScore);

        attempt.setCorrectCount(correctCount);

        attempt.setTotalQuestionCount(
                submission.getAnswers().size()
        );

        studentExamMapper.completeAttempt(attempt);

        expService.giveAttemptExp(
                studentId,
                attempt.getExamId(),
                attempt.getAttemptType(),
                attempt.getTotalScore(),
                attempt.getMaxScore()
        );
        // 일반 시험 또는 시험별 복습 결과를 경험치 기능에 전달

        return attempt.getAttemptId();
    }

    private boolean checkSubmittedAnswer(
            ExamQuestion question,
            String studentAnswer
    ) {

        if (question == null || studentAnswer == null) {
            return false;
        }

        String correctAnswer =
                question.getCorrectAnswer();

        if (correctAnswer == null) {
            return false;
        }

        String normalizedStudentAnswer =
                studentAnswer.trim();

        // 객관식
        if ("MULTIPLE_CHOICE".equalsIgnoreCase(
                question.getQuestionType()
        )) {

            // 일반 시험처럼 번호가 넘어온 경우
            if (correctAnswer.trim().equalsIgnoreCase(
                    normalizedStudentAnswer
            )) {
                return true;
            }

            // 게임형 시험처럼 선지 텍스트가 넘어온 경우
            List<ExamQuestionChoice> choices =
                    studentExamMapper.findChoices(
                            question.getQuestionId()
                    );

            if (choices == null || choices.isEmpty()) {
                return false;
            }

            ExamQuestionChoice correctChoice =
                    choices.stream()
                            .filter(choice ->
                                    String.valueOf(
                                            choice.getChoiceNumber()
                                    ).equals(
                                            correctAnswer.trim()
                                    )
                            )
                            .findFirst()
                            .orElse(null);

            if (correctChoice == null
                    || correctChoice.getChoiceText() == null) {
                return false;
            }

            return correctChoice
                    .getChoiceText()
                    .trim()
                    .equalsIgnoreCase(
                            normalizedStudentAnswer
                    );
        }

        // 주관식
        return correctAnswer
                .trim()
                .equalsIgnoreCase(
                        normalizedStudentAnswer
                );
    }

    private boolean checkAnswer(
            String correctAnswer,
            String studentAnswer) {

        if (correctAnswer == null ||
                studentAnswer == null) {
            return false;
        }

        return correctAnswer
                .trim()
                .equalsIgnoreCase(
                        studentAnswer.trim()
                );
    }

    public ExamAttempt getResult(
            Long attemptId,
            Long studentId) {

        ExamAttempt attempt =
                studentExamMapper.findAttemptById(
                        attemptId
                );

        if (attempt == null) {
            throw new IllegalArgumentException(
                    "시험 결과가 존재하지 않습니다."
            );
        }

        if (!attempt.getStudentId()
                .equals(studentId)) {

            throw new IllegalStateException(
                    "본인의 시험 결과만 확인할 수 있습니다."
            );
        }

        return attempt;
    }

    public List<ExamAnswer> getResultAnswers(
            Long attemptId) {

        return studentExamMapper
                .findAnswersByAttemptId(
                        attemptId
                );
    }

    public Paging getStudentResultPaging(
            Long studentId,
            LocalDate date,
            int page
    ) {

        int totalCount =
                studentExamMapper.countStudentResults(
                        studentId,
                        date
                );

        return new Paging(
                page,
                PAGE_SIZE,
                totalCount
        );
    }

    public List<StudentExamResult>
    getStudentExamResults(
            Long studentId,
            LocalDate date,
            Paging paging
    ) {

        return studentExamMapper.findStudentResults(
                studentId,
                date,
                paging.getPageSize(),
                paging.getOffset()
        );
    }

    public StudentGrowthSummary getStudentGrowthSummary(
            Long studentId
    ) {

        LocalDate periodEnd = LocalDate.now();
        LocalDate periodStart = periodEnd.minusDays(27);
        LocalDate recentStart = periodEnd.minusDays(13);

        StudentGrowthSummary summary =
                studentExamMapper.findStudentGrowthSummary(
                        studentId,
                        periodStart,
                        recentStart,
                        periodEnd
                );

        if (summary == null) {
            summary = new StudentGrowthSummary();
        }

        summary.setPeriodStart(periodStart);
        summary.setPeriodEnd(periodEnd);

        int previousScore = safeInt(
                summary.getPreviousAverageScore()
        );
        int recentScore = safeInt(
                summary.getRecentAverageScore()
        );
        int reviewCorrectCount = safeInt(
                summary.getReviewCorrectCount()
        );
        int reviewQuestionCount = safeInt(
                summary.getReviewQuestionCount()
        );

        summary.setPreviousAverageScore(previousScore);
        summary.setRecentAverageScore(recentScore);
        summary.setScoreChange(recentScore - previousScore);
        summary.setPreviousAttemptCount(
                safeInt(summary.getPreviousAttemptCount())
        );
        summary.setRecentAttemptCount(
                safeInt(summary.getRecentAttemptCount())
        );
        summary.setCompletedAttemptCount(
                safeInt(summary.getCompletedAttemptCount())
        );
        summary.setReviewCorrectCount(reviewCorrectCount);
        summary.setReviewQuestionCount(reviewQuestionCount);

        int retrySuccessRate = reviewQuestionCount == 0
                ? 0
                : (int) Math.round(
                reviewCorrectCount * 100.0
                        / reviewQuestionCount
        );

        summary.setRetrySuccessRate(retrySuccessRate);

        List<LocalDate> studyDates =
                studentExamMapper.findStudentGrowthStudyDates(
                        studentId,
                        periodStart,
                        periodEnd
                );

        summary.setStudyDays(studyDates.size());
        summary.setLongestStreak(
                calculateLongestStreak(studyDates)
        );

        return summary;
    }

    private int calculateLongestStreak(
            List<LocalDate> studyDates
    ) {

        int longestStreak = 0;
        int currentStreak = 0;
        LocalDate previousDate = null;

        for (LocalDate studyDate : studyDates) {
            if (previousDate != null
                    && studyDate.equals(previousDate.plusDays(1))) {
                currentStreak++;
            } else {
                currentStreak = 1;
            }

            longestStreak = Math.max(
                    longestStreak,
                    currentStreak
            );
            previousDate = studyDate;
        }

        return longestStreak;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    public int getTodayReviewCount(Long studentId) {

        return studentExamMapper
                .countTodayReviewQuestions(studentId);
    }

    public List<ExamQuestion> getTodayReviewQuestions(
            Long studentId
    ) {

        // 1. 원본 오답만 조회
        List<ExamQuestion> originalQuestions =
                studentExamMapper.findTodayOriginalWrongQuestions(studentId);

        // 2. 원본 문제 선택지 로딩
        loadChoices(originalQuestions);

        // 3. 각 원본 문제별 AI 문제 생성
        for (ExamQuestion original :
                originalQuestions) {

            try {
                aiReviewQuestionService.getOrCreate(original);
            } catch (Exception e) {

                /*
                 * AI 문제 생성에 실패해도
                 * 기존 오늘의 복습은 실행 가능하게 한다.
                 */
                log.error(
                        "AI 문제 생성 실패 - questionId={}",
                        original.getQuestionId(),
                        e
                );
            }
        }

        // 4. 원본 + AI 문제 최종 조회
        List<ExamQuestion> reviewQuestions = studentExamMapper.findTodayReviewQuestions(studentId);

        // 5. 전체 문제 선택지 로딩
        loadChoices(reviewQuestions);

        return reviewQuestions;
    }

    /**
     * 오늘의 복습 시작
     */
    @Transactional
    public DailyReviewAttempt startTodayReview(
            Long studentId) {

        DailyReviewAttempt existing =
                studentExamMapper
                        .findTodayInProgressReview(
                                studentId
                        );

        if (existing != null) {
            return existing;
        }

        List<ExamQuestion> questions =
                getTodayReviewQuestions(studentId);

        if (questions.isEmpty()) {
            throw new IllegalStateException(
                    "오늘 복습할 문제가 없습니다."
            );
        }

        BigDecimal maxScore =
                questions.stream()
                        .map(ExamQuestion::getScore)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        DailyReviewAttempt attempt =
                new DailyReviewAttempt();

        attempt.setStudentId(studentId);

        attempt.setMaxScore(maxScore);

        attempt.setTotalQuestionCount(
                questions.size()
        );

        studentExamMapper
                .insertDailyReviewAttempt(
                        attempt
                );

        return attempt;
    }

    /**
     * 오늘의 복습 제출
     */

    @Transactional
    public Long submitTodayReview(
            Long studentId,
            DailyReviewSubmission submission) {

        List<ExamQuestion> reviewQuestions =
                studentExamMapper
                        .findTodayReviewQuestions(
                                studentId
                        );

        int correctCount = 0;

        BigDecimal totalScore =
                BigDecimal.ZERO;

        BigDecimal maxScore =
                BigDecimal.ZERO;

        for (DailyReviewAnswer answer
                : submission.getAnswers()) {

            ExamQuestion question =
                    reviewQuestions.stream()
                            .filter(q ->
                                    q.getQuestionId()
                                            .equals(
                                                    answer.getQuestionId()
                                            )
                            )
                            .findFirst()
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "오늘의 복습 대상 문제가 아닙니다."
                                    )
                            );

            boolean correct =
                    checkSubmittedAnswer(
                            question,
                            answer.getStudentAnswer()
                    );

            BigDecimal earnedScore =
                    correct
                            ? question.getScore()
                            : BigDecimal.ZERO;

            answer.setDailyReviewAttemptId(
                    submission.getDailyReviewAttemptId()
            );

            answer.setIsCorrect(correct);
            answer.setEarnedScore(earnedScore);

            studentExamMapper
                    .insertDailyReviewAnswer(
                            answer
                    );

            maxScore =
                    maxScore.add(
                            question.getScore()
                    );

            totalScore =
                    totalScore.add(
                            earnedScore
                    );

            if (correct) {
                correctCount++;
            }
        }

        DailyReviewAttempt attempt =
                new DailyReviewAttempt();

        attempt.setDailyReviewAttemptId(
                submission.getDailyReviewAttemptId()
        );

        attempt.setTotalScore(totalScore);
        attempt.setMaxScore(maxScore);

        attempt.setCorrectCount(correctCount);

        attempt.setTotalQuestionCount(
                submission.getAnswers().size()
        );

        studentExamMapper
                .completeDailyReviewAttempt(
                        attempt
                );

        expService.giveDailyReviewExp(
                studentId,
                LocalDate.now(),
                correctCount,
                submission.getAnswers().size(),
                reviewQuestions.size()
        );
        // 전체 문제 완료·정답률 80% 이상·오늘 최초 1회이면 20점 지급

        return submission
                .getDailyReviewAttemptId();
    }

    public DailyReviewAttempt getDailyReviewResult(
            Long dailyReviewAttemptId,
            Long studentId) {

        DailyReviewAttempt attempt =
                studentExamMapper
                        .findDailyReviewAttemptById(
                                dailyReviewAttemptId
                        );

        if (attempt == null) {
            throw new IllegalArgumentException(
                    "오늘의 복습 결과가 존재하지 않습니다."
            );
        }

        if (!attempt.getStudentId()
                .equals(studentId)) {

            throw new IllegalStateException(
                    "본인의 복습 결과만 확인할 수 있습니다."
            );
        }

        return attempt;
    }

    public List<DailyReviewAnswer>
    getDailyReviewResultAnswers(
            Long dailyReviewAttemptId) {

        return studentExamMapper
                .findDailyReviewAnswers(
                        dailyReviewAttemptId
                );
    }

    /**
     * 단어시험 게임형
     */
    @Transactional
    public List<ExamQuestion> getWordGameReviewQuestions(
            Long examId,
            Long studentId
    ) {

        Exam exam =
                studentExamMapper.findExamById(examId);

        if (exam == null) {
            throw new IllegalArgumentException(
                    "시험이 존재하지 않습니다."
            );
        }

        if (!"WORD".equalsIgnoreCase(exam.getExamType())) {
            throw new IllegalArgumentException(
                    "단어 시험이 아닙니다."
            );
        }

        ExamAttempt originalAttempt =
                studentExamMapper.findLatestOriginalAttempt(
                        examId,
                        studentId
                );

        if (originalAttempt == null) {
            throw new IllegalStateException(
                    "먼저 단어 시험에 응시해주세요."
            );
        }

        List<ExamQuestion> questions =
                studentExamMapper.findWrongQuestions(
                        originalAttempt.getAttemptId()
                );

        if (questions.isEmpty()) {
            throw new IllegalStateException(
                    "복습할 오답이 없습니다."
            );
        }

        loadChoices(questions);

        return questions;
    }

    @Transactional
    public ExamAttempt startWordGameReview(
            Long examId,
            Long studentId,
            List<ExamQuestion> questions
    ) {

        Exam exam =
                studentExamMapper.findExamById(examId);

        if (exam == null) {
            throw new IllegalArgumentException(
                    "시험이 존재하지 않습니다."
            );
        }

        if (!"WORD".equalsIgnoreCase(exam.getExamType())) {
            throw new IllegalArgumentException(
                    "단어 시험이 아닙니다."
            );
        }

        ExamAttempt sourceAttempt =
                studentExamMapper.findLatestOriginalAttempt(
                        examId,
                        studentId
                );

        if (sourceAttempt == null) {
            throw new IllegalStateException(
                    "단어 시험 응시 기록이 없습니다."
            );
        }

        return createAttempt(
                examId,
                studentId,
                "REVIEW",
                sourceAttempt.getAttemptId(),
                questions
        );
    }

    public boolean checkWordGameAnswer(
            Long studentId,
            Long attemptId,
            Long questionId,
            String studentAnswer
    ) {

        ExamAttempt attempt =
                studentExamMapper.findAttemptById(
                        attemptId
                );

        if (attempt == null) {
            throw new IllegalArgumentException(
                    "응시 기록이 존재하지 않습니다."
            );
        }

        if (!attempt.getStudentId().equals(studentId)) {
            throw new IllegalStateException(
                    "본인의 시험만 응시할 수 있습니다."
            );
        }

        if (!"EXAM".equalsIgnoreCase(attempt.getAttemptType())) {
            throw new IllegalStateException(
                    "시험 응시 기록이 아닙니다."
            );
        }

        if (!"GAME".equalsIgnoreCase(attempt.getEntryMethod())) {
            throw new IllegalStateException(
                    "게임형 시험이 아닙니다."
            );
        }

        if (!"IN_PROGRESS".equalsIgnoreCase(attempt.getStatus())) {
            throw new IllegalStateException(
                    "이미 완료된 시험입니다."
            );
        }

        Exam exam =
                studentExamMapper.findExamById(
                        attempt.getExamId()
                );

        if (exam == null || !"WORD".equalsIgnoreCase(exam.getExamType())) {
            throw new IllegalStateException(
                    "단어 시험이 아닙니다."
            );
        }

        ExamQuestion question =
                studentExamMapper.findQuestionById(
                        questionId
                );

        if (question == null) {
            throw new IllegalArgumentException(
                    "문제가 존재하지 않습니다."
            );
        }

        if (!question.getExamId().equals(attempt.getExamId())) {
            throw new IllegalStateException(
                    "해당 시험의 문제가 아닙니다."
            );
        }

        // =========================================
        // 객관식이면 정답 번호 -> 정답 보기 텍스트로 변환 후 비교
        // =========================================
        if ("MULTIPLE_CHOICE".equalsIgnoreCase(question.getQuestionType())) {

            List<ExamQuestionChoice> choices =
                    studentExamMapper.findChoices(questionId);

            String correctChoiceText = choices.stream()
                    .filter(choice ->
                            String.valueOf(choice.getChoiceNumber())
                                    .equals(question.getCorrectAnswer())
                    )
                    .map(ExamQuestionChoice::getChoiceText)
                    .findFirst()
                    .orElseThrow(() ->
                            new IllegalStateException("정답 보기를 찾을 수 없습니다.")
                    );

            return checkAnswer(correctChoiceText, studentAnswer);
        }

        // =========================================
        // 주관식이면 기존 방식 그대로
        // =========================================
        return checkAnswer(
                question.getCorrectAnswer(),
                studentAnswer
        );
    }

    @Transactional
    public ExamAttempt startWordGameExam(
            Long examId,
            Long studentId
    ) {

        Exam exam =
                studentExamMapper.findExamById(
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
                    "단어 시험이 아닙니다."
            );
        }

        List<ExamQuestion> questions =
                studentExamMapper.findAllQuestions(
                        examId
                );

        return createGameAttempt(
                examId,
                studentId,
                questions
        );
    }

    private ExamAttempt createGameAttempt(
            Long examId,
            Long studentId,
            List<ExamQuestion> questions
    ) {

        BigDecimal maxScore =
                questions.stream()
                        .map(ExamQuestion::getScore)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        int nextAttemptNo =
                studentExamMapper.findNextAttemptNo(
                        examId,
                        studentId
                );

        ExamAttempt attempt =
                new ExamAttempt();

        attempt.setExamId(
                examId
        );

        attempt.setStudentId(
                studentId
        );

        attempt.setAttemptNo(
                nextAttemptNo
        );

        // 복습이 아니라 실제 시험
        attempt.setAttemptType(
                "EXAM"
        );

        attempt.setSourceAttemptId(
                null
        );

        // 게임형 응시 표시
        attempt.setEntryMethod(
                "GAME"
        );

        attempt.setMaxScore(
                maxScore
        );

        attempt.setTotalQuestionCount(
                questions.size()
        );

        studentExamMapper.insertAttempt(
                attempt
        );

        return attempt;
    }

    public List<String> getWordGameOptions(
            Long studentId,
            Long attemptId,
            Long questionId
    ) {

        ExamAttempt attempt =
                studentExamMapper.findAttemptById(
                        attemptId
                );

        if (attempt == null) {
            throw new IllegalArgumentException(
                    "응시 기록이 존재하지 않습니다."
            );
        }

        if (!attempt.getStudentId().equals(studentId)) {
            throw new IllegalStateException(
                    "본인의 시험만 응시할 수 있습니다."
            );
        }

        if (!"EXAM".equalsIgnoreCase(
                attempt.getAttemptType()
        )) {
            throw new IllegalStateException(
                    "시험 응시 기록이 아닙니다."
            );
        }

        if (!"GAME".equalsIgnoreCase(
                attempt.getEntryMethod()
        )) {
            throw new IllegalStateException(
                    "게임형 시험이 아닙니다."
            );
        }

        ExamQuestion targetQuestion =
                studentExamMapper.findQuestionById(
                        questionId
                );

        if (targetQuestion == null) {
            throw new IllegalArgumentException(
                    "문제가 존재하지 않습니다."
            );
        }

        if (!targetQuestion.getExamId().equals(attempt.getExamId())) {
            throw new IllegalStateException(
                    "해당 시험의 문제가 아닙니다."
            );
        }

        // ================================
        // 객관식인 경우
        // -> 해당 문제의 choiceText를 그대로 사용
        // ================================
        if ("MULTIPLE_CHOICE".equalsIgnoreCase(targetQuestion.getQuestionType())) {

            List<ExamQuestionChoice> choices =
                    studentExamMapper.findChoices(questionId);

            if (choices == null || choices.isEmpty()) {
                throw new IllegalStateException(
                        "객관식 보기 정보가 존재하지 않습니다."
                );
            }

            List<String> options = new ArrayList<>();

            for (ExamQuestionChoice choice : choices) {
                if (choice.getChoiceText() != null
                        && !choice.getChoiceText().isBlank()) {

                    options.add(choice.getChoiceText().trim());
                }
            }

            Collections.shuffle(options);

            return options;
        }

        // ================================
        // 주관식인 경우
        // -> 정답 + 다른 문제 정답 3개
        // ================================
        List<ExamQuestion> allQuestions =
                studentExamMapper.findAllQuestions(
                        attempt.getExamId()
                );

        List<String> options =
                new ArrayList<>();

        String correctAnswer =
                targetQuestion.getCorrectAnswer() == null
                        ? ""
                        : targetQuestion.getCorrectAnswer().trim();

        if (correctAnswer.isBlank()) {
            throw new IllegalStateException(
                    "정답 정보가 존재하지 않습니다."
            );
        }

        options.add(correctAnswer);

        List<String> wrongAnswers =
                allQuestions.stream()
                        .filter(q -> !q.getQuestionId().equals(questionId))
                        .map(ExamQuestion::getCorrectAnswer)
                        .filter(answer -> answer != null && !answer.isBlank())
                        .map(String::trim)
                        .filter(answer -> !answer.equalsIgnoreCase(correctAnswer))
                        .distinct()
                        .collect(
                                java.util.stream.Collectors.toCollection(
                                        ArrayList::new
                                )
                        );

        Collections.shuffle(wrongAnswers);

        int wrongOptionCount = Math.min(3, wrongAnswers.size());

        for (int i = 0; i < wrongOptionCount; i++) {
            options.add(wrongAnswers.get(i));
        }

        Collections.shuffle(options);

        return options;
    }

    public List<Map<String, Object>>
    getWordMatchingPairs(
            Long studentId,
            Long attemptId
    ) {

        ExamAttempt attempt =
                studentExamMapper.findAttemptById(
                        attemptId
                );

        if (attempt == null) {
            throw new IllegalArgumentException(
                    "응시 기록이 존재하지 않습니다."
            );
        }

        if (!attempt.getStudentId()
                .equals(studentId)) {

            throw new IllegalStateException(
                    "본인의 시험만 응시할 수 있습니다."
            );
        }

        if (!"GAME".equalsIgnoreCase(
                attempt.getEntryMethod()
        )) {

            throw new IllegalStateException(
                    "게임형 시험이 아닙니다."
            );
        }

        List<ExamQuestion> questions =
                studentExamMapper
                        .findAllQuestions(
                                attempt.getExamId()
                        );

        List<Map<String, Object>> pairs =
                new ArrayList<>();


        for (ExamQuestion question : questions) {

            String answerText;


            // ==========================
            // 객관식
            // ==========================

            if ("MULTIPLE_CHOICE".equalsIgnoreCase(
                    question.getQuestionType()
            )) {

                List<ExamQuestionChoice> choices =
                        studentExamMapper
                                .findChoices(
                                        question.getQuestionId()
                                );

                String correctAnswer =
                        question.getCorrectAnswer();

                answerText =
                        choices.stream()
                                .filter(choice ->
                                        String.valueOf(
                                                choice.getChoiceNumber()
                                        ).equals(
                                                correctAnswer
                                                        .trim()
                                        )
                                )
                                .map(
                                        ExamQuestionChoice::getChoiceText
                                )
                                .findFirst()
                                .orElse(
                                        correctAnswer
                                );

            }

            // ==========================
            // 주관식
            // ==========================

            else {

                answerText =
                        question.getCorrectAnswer();
            }


            if (answerText == null
                    || answerText.isBlank()) {

                continue;
            }


            Map<String, Object> pair =
                    new HashMap<>();

            pair.put(
                    "questionId",
                    question.getQuestionId()
            );

            pair.put(
                    "word",
                    question.getQuestionText()
            );

            pair.put(
                    "answer",
                    answerText.trim()
            );

            pairs.add(
                    pair
            );
        }


        return pairs;
    }

}