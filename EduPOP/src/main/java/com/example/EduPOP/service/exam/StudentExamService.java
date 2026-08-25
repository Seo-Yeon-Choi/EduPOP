package com.example.EduPOP.service.exam;

import com.example.EduPOP.domain.common.Paging;
import com.example.EduPOP.domain.exam.*;
import com.example.EduPOP.repository.exam.StudentExamMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentExamService {

    private final StudentExamMapper studentExamMapper;

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

            if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {

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

        if (exam.getExamType()!=ExamType.WORD) {
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
                    checkAnswer(
                            question.getCorrectAnswer(),
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

        return attempt.getAttemptId();
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

    public int getTodayReviewCount(Long studentId) {

        return studentExamMapper
                .countTodayReviewQuestions(studentId);
    }

    public List<ExamQuestion> getTodayReviewQuestions(
            Long studentId) {

        List<ExamQuestion> questions =
                studentExamMapper
                        .findTodayReviewQuestions(studentId);

        loadChoices(questions);

        return questions;
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
                studentExamMapper
                        .findTodayReviewQuestions(
                                studentId
                        );

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
                    checkAnswer(
                            question.getCorrectAnswer(),
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
}