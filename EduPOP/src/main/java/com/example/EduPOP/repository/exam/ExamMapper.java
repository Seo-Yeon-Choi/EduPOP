package com.example.EduPOP.repository.exam;

import com.example.EduPOP.domain.exam.Exam;
import com.example.EduPOP.domain.exam.StudentExam;
import com.example.EduPOP.domain.exam.StudentExamResult;
import com.example.EduPOP.controller.exam.dto.*;
import com.example.EduPOP.domain.exam.ExamAttempt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ExamMapper {

    // 템플릿 목록 조회
    List<ExamTemplateResponse> findTemplatesByAcademyId(@Param("academyId") Long academyId);

    // 신규 시험지 생성
    int insertExam(ExamCreateOrCopyRequest request);

    // 템플릿 문항 복제
    int copyQuestionsFromTemplate(@Param("newExamId") Long newExamId, @Param("templateExamId") Long templateExamId);

    // 직접 등록 문항 일괄 저장
    void batchInsertCustomQuestions(
            @Param("examId") Long examId,
            @Param("questions") List<ExamCreateOrCopyRequest.CustomQuestionDto> questions
    );

    // 전체 시험 목록 조회
    List<ExamListResponse> findAllExams();

    // 시험 기본 정보 조회
    ExamDetailResponse findExamBaseInfoById(@Param("examId") Long examId);

    // 시험 문항 목록 조회
    List<ExamDetailResponse.QuestionMetaInfo> findQuestionsByExamId(@Param("examId") Long examId);

    // 반 소속 학생 명단 조회
    List<ExamDetailResponse.StudentRosterDto> findStudentsByClassId(@Param("classId") Long classId);
    int insert(Exam exam);

    // 학생 응시 원장 저장
    int insertExamAttempt(ExamAttempt attempt);
    int updateExamAttemptScore(ExamAttempt attempt);
    Exam findById(Long examId);

    // 문항별 세부 답안 일괄 저장
    int batchInsertExamAnswers(@Param("attemptId") Long attemptId, @Param("answers") List<Map<String, Object>> answers);
    int update(Exam exam);

    List<ExamDetailResponse.StudentRosterDto> findStudentsByExamAndClassId(@Param("examId") Long examId, @Param("classId") Long classId);
    int delete(Long examId);

    // 기존 응시 원장 조회
    ExamAttempt findExamAttempt(@Param("examId") Long examId, @Param("studentId") Long studentId);
    void deleteExamAnswersByAttemptId(@Param("attemptId") Long attemptId);
    void deleteExamAttemptByExamAndStudent(@Param("examId") Long examId, @Param("studentId") Long studentId);
    List<Exam> findAll();

    int updateTeacherComment(
            @Param("examId") Long examId,
            @Param("studentId") Long studentId,
            @Param("teacherId") Long teacherId,
            @Param("comment") String comment
    );

    int insertTeacherComment(
            @Param("examId") Long examId,
            @Param("studentId") Long studentId,
            @Param("teacherId") Long teacherId,
            @Param("comment") String comment
    );
    List<Exam> findByTeacherId(Long teacherId);


    // 평균 통계
    Map<String, Object> getExamStats(@Param("examId") Long examId, @Param("classId") Long classId);
    List<StudentExamResult> findDailyReviewResults(Long studentId);
}
