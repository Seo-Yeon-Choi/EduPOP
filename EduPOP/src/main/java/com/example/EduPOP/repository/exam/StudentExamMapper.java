package com.example.EduPOP.repository.exam;

import com.example.EduPOP.domain.exam.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface StudentExamMapper {

    List<StudentExam> findStudentExams(
            @Param("studentId") Long studentId,
            @Param("date") LocalDate date,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    Exam findExamById(Long examId);

    List<ExamQuestion> findAllQuestions(Long examId);

    List<ExamQuestion> findWrongQuestions(
            @Param("attemptId") Long attemptId
    );

    List<ExamQuestionChoice> findChoices(Long questionId);

    ExamQuestion findQuestionById(Long questionId);

    ExamAttempt findLatestOriginalAttempt(
            @Param("examId") Long examId,
            @Param("studentId") Long studentId
    );

    ExamAttempt findInProgressAttempt(
            @Param("examId") Long examId,
            @Param("studentId") Long studentId,
            @Param("attemptType") String attemptType
    );

    int countStudentExams(
            @Param("studentId") Long studentId,
            @Param("date") LocalDate date
    );

    int findNextAttemptNo(
            @Param("examId") Long examId,
            @Param("studentId") Long studentId
    );

    int insertAttempt(ExamAttempt attempt);

    int insertAnswer(ExamAnswer answer);

    int completeAttempt(ExamAttempt attempt);

    ExamAttempt findAttemptById(Long attemptId);

    List<ExamAnswer> findAnswersByAttemptId(Long attemptId);

    List<StudentExamResult> findStudentResults(
            @Param("studentId") Long studentId,
            @Param("date") LocalDate date,
            @Param("limit") int limit,
            @Param("offset") int offset
    );
    /**
     * 오늘의 복습 항목
     */
    List<ExamQuestion> findTodayReviewQuestions(Long studentId);

    int countTodayReviewQuestions(Long studentId);

    DailyReviewAttempt findTodayInProgressReview(@Param("studentId") Long studentId);

    int insertDailyReviewAttempt(DailyReviewAttempt attempt);

    int insertDailyReviewAnswer(DailyReviewAnswer answer);

    int completeDailyReviewAttempt(DailyReviewAttempt attempt);

    /**
     * 오늘의 복습 결과 조회
     */
    DailyReviewAttempt findDailyReviewAttemptById(Long dailyReviewAttemptId);

    List<DailyReviewAnswer> findDailyReviewAnswers(Long dailyReviewAttemptId);

    // 오늘의 복습 결과 목록을 따로 조회하기 위해 새로 추가
    int countStudentResults(
            @Param("studentId") Long studentId,
            @Param("date") LocalDate date
    );

    StudentGrowthSummary findStudentGrowthSummary(
            @Param("studentId") Long studentId,
            @Param("periodStart") LocalDate periodStart,
            @Param("recentStart") LocalDate recentStart,
            @Param("periodEnd") LocalDate periodEnd
    );

    List<LocalDate> findStudentGrowthStudyDates(
            @Param("studentId") Long studentId,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd
    );
}
