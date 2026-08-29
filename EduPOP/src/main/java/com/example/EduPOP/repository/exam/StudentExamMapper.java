package com.example.EduPOP.repository.exam;

import com.example.EduPOP.domain.exam.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
    // =========================================================
    // 기존 팀원이 만들어놓은 학습 참여 날짜 조회
    //
    // 시험 완료 날짜 + 오늘의 복습 완료 날짜를 합쳐
    // 실제 학습한 날짜를 중복 제거해서 반환한다.
    // =========================================================


    // =========================================================
// 월간 시험 응시율
//
// 해당 기간의 시험 중
// 학생이 EXAM 타입으로 실제 응시하고 GRADED까지 완료한
// 시험의 비율을 계산한다.
// =========================================================

    @org.apache.ibatis.annotations.Select("""
    SELECT
        COALESCE(
            ROUND(
                COUNT(
                    DISTINCT CASE
                        WHEN ea.attempt_id IS NOT NULL
                        THEN e.exam_id
                    END
                ) * 100.0
                /
                NULLIF(
                    COUNT(DISTINCT e.exam_id),
                    0
                ),
                2
            ),
            0
        )
    FROM exams e

    INNER JOIN class_students cs
        ON cs.class_id = e.class_id
       AND cs.student_id = #{studentId}
       AND cs.status = 'ACTIVE'

    LEFT JOIN exam_attempts ea
        ON ea.exam_id = e.exam_id
       AND ea.student_id = #{studentId}
       AND ea.attempt_type = 'EXAM'
       AND ea.status = 'GRADED'

    WHERE e.exam_date
          BETWEEN #{periodStart}
          AND #{periodEnd}
      AND e.exam_date <= CURDATE()
    """)
    Double findExamCompletionRate(
            @org.apache.ibatis.annotations.Param("studentId")
            Long studentId,

            @org.apache.ibatis.annotations.Param("periodStart")
            LocalDate periodStart,

            @org.apache.ibatis.annotations.Param("periodEnd")
            LocalDate periodEnd
    );


// =========================================================
// 월간 재시험 응시율
//
// 네가 말한 기준대로
// exam_attempts.attempt_no >= 2
// 를 재시험으로 간주한다.
//
// 예:
// attempt_no = 1 → 최초 시험
// attempt_no = 2 → 1차 재시험
// attempt_no = 3 → 2차 재시험
//
// 해당 기간의 시험 중 재시험(attempt_no >= 2)이
// 발생한 시험의 비율을 계산한다.
// =========================================================

    @org.apache.ibatis.annotations.Select("""
    SELECT
        COALESCE(
            ROUND(
                COUNT(
                    DISTINCT CASE
                        WHEN ea.attempt_no >= 2
                        THEN e.exam_id
                    END
                ) * 100.0
                /
                NULLIF(
                    COUNT(DISTINCT e.exam_id),
                    0
                ),
                2
            ),
            0
        )
    FROM exams e

    INNER JOIN class_students cs
        ON cs.class_id = e.class_id
       AND cs.student_id = #{studentId}
       AND cs.status = 'ACTIVE'

    LEFT JOIN exam_attempts ea
        ON ea.exam_id = e.exam_id
       AND ea.student_id = #{studentId}
       AND ea.status = 'GRADED'

    WHERE e.exam_date
          BETWEEN #{periodStart}
          AND #{periodEnd}
    """)
    Double findRetestCompletionRate(
            @org.apache.ibatis.annotations.Param("studentId")
            Long studentId,

            @org.apache.ibatis.annotations.Param("periodStart")
            LocalDate periodStart,

            @org.apache.ibatis.annotations.Param("periodEnd")
            LocalDate periodEnd
    );
}
