package com.example.EduPOP.repository.report;

import com.example.EduPOP.dto.ReportMetricsDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface ReportMapper {

    // =========================================================
    // 사용자 이름 조회
    // =========================================================

    @Select("""
        SELECT name
        FROM users
        WHERE user_id = #{userId}
        """)
    String getUserNameById(
            @Param("userId") Long userId
    );


    // =========================================================
    // 현재 학생이 속한 ACTIVE 반 이름 조회
    // =========================================================

    @Select("""
        SELECT c.name
        FROM classes c
        JOIN class_students cs
          ON cs.class_id = c.class_id
        WHERE cs.student_id = #{studentId}
          AND cs.status = 'ACTIVE'
        ORDER BY cs.class_student_id DESC
        LIMIT 1
        """)
    String getClassNameByStudentId(
            @Param("studentId") Long studentId
    );


    // =========================================================
    // 학부모 리포트 기본 통계
    //
    // 실제 DB 데이터를 조회한다.
    //
    // 조회 대상:
    // 1. 학생 이름
    // 2. 학원 이름
    // 3. 반 이름
    // 4. 선생님 이름
    // 5. 월말평가 점수
    // 6. 반 평균
    // 7. 단어 시험 응시율
    // 8. 읽은 도서 수
    // 9. 오답 극복 수
    // 10. 최다 오답 유형
    // =========================================================

    ReportMetricsDTO getMonthlyReportMetrics(
            @Param("studentId") Long studentId,
            @Param("teacherId") Long teacherId,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd
    );


    // =========================================================
    // 학부모 전화번호 조회
    //
    // 학생과 연결된 PARENT 사용자의 전화번호를 조회한다.
    //
    // 학부모 인증/URL 기능에서 기존 방식 그대로 사용한다.
    // =========================================================

    @Select("""
        SELECT p.phone
        FROM parent_students ps
        JOIN users p
          ON p.user_id = ps.parent_id
        WHERE ps.student_id = #{studentId}
          AND p.role = 'PARENT'
        LIMIT 1
        """)
    String getParentPhoneByStudentId(
            @Param("studentId") Long studentId
    );


    // =========================================================
    // 반 + 학생 이름으로 학생 ID 조회
    //
    // URL 관련 인증 기능이 사용하므로 그대로 유지한다.
    // =========================================================

    @Select("""
        SELECT u.user_id
        FROM users u
        JOIN class_students cs
          ON cs.student_id = u.user_id
        WHERE cs.class_id = #{classId}
          AND u.name = #{studentName}
          AND cs.status = 'ACTIVE'
        LIMIT 1
        """)
    Long findStudentIdByClassAndName(
            @Param("classId") Long classId,
            @Param("studentName") String studentName
    );


    // =========================================================
    // 가장 최근 발행 리포트의 access_token
    //
    // URL 관련 기능은 그대로 유지한다.
    // =========================================================

    @Select("""
        SELECT access_token
        FROM parent_reports
        WHERE student_id = #{studentId}
          AND status = 'PUBLISHED'
        ORDER BY created_at DESC
        LIMIT 1
        """)
    String findLatestPublishedReportToken(
            @Param("studentId") Long studentId
    );


    // =========================================================
    // ⭐ 동적 영역별 성취도
    //
    // 영역 이름을 절대 하드코딩하지 않는다.
    //
    // 시험 생성 시 선생님이 입력한
    // exam_sections.section_name
    //
    // 을 그대로 사용한다.
    //
    // 따라서:
    //
    // 듣기 / 쓰기 / 독해
    //
    // 3개를 입력하면 3개.
    //
    // 듣기 / 어휘 / 문법 / 독해 / 쓰기
    //
    // 5개를 입력하면 5개.
    //
    // 학생의 실제 시험 답안
    // exam_answers.earned_score
    //
    // 와
    //
    // exam_questions.score
    //
    // 를 사용하여 영역별 성취도를 계산한다.
    // =========================================================

    List<Map<String, Object>> findSectionAchievementScores(
            @Param("studentId") Long studentId,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd
    );


    // =========================================================
    // 월말평가 점수 추이
    //
    // 실제 exams + exam_attempts를 이용한다.
    //
    // 최근 12개월 동안의 월말평가를 조회한다.
    // =========================================================

    List<Map<String, Object>> findMonthlyScoreTrend(
            @Param("studentId") Long studentId
    );


    // =========================================================
    // 오답 극복 문제 수
    // 원래 틀린 문항을 시험별 복습 또는 오늘의 복습에서 맞힌 경우를 집계한다.
    // =========================================================

    @Select("""
        SELECT COUNT(*)
        FROM (
            SELECT DISTINCT review_answer.question_id
            FROM exam_attempts review_attempt
            JOIN exam_answers review_answer
              ON review_answer.attempt_id = review_attempt.attempt_id
            WHERE review_attempt.student_id = #{studentId}
              AND review_attempt.attempt_type = 'REVIEW'
              AND review_attempt.status = 'GRADED'
              AND review_answer.is_correct = TRUE
              AND DATE(review_attempt.graded_at)
                    BETWEEN #{periodStart} AND #{periodEnd}
              AND EXISTS (
                    SELECT 1
                    FROM exam_answers original_answer
                    WHERE original_answer.attempt_id = review_attempt.source_attempt_id
                      AND original_answer.question_id = review_answer.question_id
                      AND original_answer.is_correct = FALSE
              )

            UNION

            SELECT DISTINCT daily_answer.question_id
            FROM daily_review_attempts daily_attempt
            JOIN daily_review_answers daily_answer
              ON daily_answer.daily_review_attempt_id =
                 daily_attempt.daily_review_attempt_id
            WHERE daily_attempt.student_id = #{studentId}
              AND daily_attempt.status = 'GRADED'
              AND daily_answer.is_correct = TRUE
              AND DATE(daily_attempt.graded_at)
                    BETWEEN #{periodStart} AND #{periodEnd}
              AND EXISTS (
                    SELECT 1
                    FROM exam_attempts original_attempt
                    JOIN exam_answers original_answer
                      ON original_answer.attempt_id = original_attempt.attempt_id
                    WHERE original_attempt.student_id = #{studentId}
                      AND original_attempt.attempt_type = 'EXAM'
                      AND original_attempt.status = 'GRADED'
                      AND original_answer.question_id = daily_answer.question_id
                      AND original_answer.is_correct = FALSE
              )
        ) overcome_questions
        """)
    Integer countOvercomeWrongCount(
            @Param("studentId") Long studentId,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd
    );
}
