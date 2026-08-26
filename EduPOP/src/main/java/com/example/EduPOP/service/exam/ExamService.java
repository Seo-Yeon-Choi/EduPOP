package com.example.EduPOP.service.exam;

import com.example.EduPOP.controller.exam.dto.*;
import com.example.EduPOP.domain.exam.ExamAttempt;
import com.example.EduPOP.repository.exam.ExamMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * [역할: 시험 관리 및 OMR 채점 핵심 서비스]
 * - 외부 알림 의존성 없이 순수 비즈니스 로직(시험 생성, 문항 복제, 일괄 채점 저장)만 집중 수행합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamMapper examMapper;

    @Transactional(readOnly = true)
    public List<ExamTemplateResponse> getExamTemplates(Long academyId) {
        return examMapper.findTemplatesByAcademyId(academyId);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createExamSheet(ExamCreateOrCopyRequest request) {
        if (request.getAcademyId() == null) request.setAcademyId(1L);
        if (request.getExamRound() == null) request.setExamRound(1);

        examMapper.insertExam(request);
        Long newExamId = request.getGeneratedExamId();

        if (request.getTemplateExamId() != null) {
            examMapper.copyQuestionsFromTemplate(newExamId, request.getTemplateExamId());
        } else if (request.getCustomQuestions() != null && !request.getCustomQuestions().isEmpty()) {
            examMapper.batchInsertCustomQuestions(newExamId, request.getCustomQuestions());
        }

        return newExamId;
    }

    @Transactional(readOnly = true)
    public List<ExamListResponse> getAllExamList() {
        return examMapper.findAllExams();
    }

    @Transactional(readOnly = true)
    public ExamDetailResponse getExamDetailForOmr(Long examId) {
        ExamDetailResponse detail = examMapper.findExamBaseInfoById(examId);
        if (detail == null) {
            throw new IllegalArgumentException("존재하지 않는 시험지입니다. ID: " + examId);
        }
        detail.setQuestions(examMapper.findQuestionsByExamId(examId));
        if (detail.getClassId() != null) {
            // 시험 ID를 함께 전달하여 기존 채점 답안/점수 로딩
            detail.setStudents(examMapper.findStudentsByExamAndClassId(examId, detail.getClassId()));
        }
        return detail;
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveBulkGrades(ExamBulkGradeRequest request) {
        Long examId = request.getExamId();
        log.info("시험 ID [{}] 일괄/개별 채점 저장 로직 실행", examId);

        if (request.getStudentGrades() == null || request.getStudentGrades().isEmpty()) {
            return;
        }

        for (ExamBulkGradeRequest.StudentGradePayload student : request.getStudentGrades()) {
            double totalScore = 0.0;
            double maxScore = 0.0;
            int correctCount = 0;
            Map<String, Integer> wrongTagMap = new HashMap<>();
            List<Map<String, Object>> answerList = new ArrayList<>();

            if (student.getAnswers() != null) {
                for (ExamBulkGradeRequest.AnswerPayload ans : student.getAnswers()) {
                    double score = (ans.getScore() != null) ? ans.getScore() : 5.0;
                    maxScore += score;

                    String submitted = (ans.getSubmittedAnswer() != null) ? ans.getSubmittedAnswer().trim() : "";
                    String correct = (ans.getCorrectAnswer() != null) ? ans.getCorrectAnswer().trim() : "";

                    boolean isCorrect = !submitted.isEmpty() && submitted.equalsIgnoreCase(correct);
                    double earned = isCorrect ? score : 0.0;

                    if (isCorrect) {
                        correctCount++;
                        totalScore += earned;
                    } else {
                        String tag = (ans.getQuestionType() != null && !ans.getQuestionType().isEmpty())
                                ? ans.getQuestionType() : "VOCAB";
                        wrongTagMap.put(tag, wrongTagMap.getOrDefault(tag, 0) + 1);
                    }

                    Map<String, Object> aMap = new HashMap<>();
                    aMap.put("questionId", ans.getQuestionId());
                    aMap.put("studentAnswer", submitted);
                    aMap.put("isCorrect", isCorrect ? 1 : 0);
                    aMap.put("earnedScore", earned);
                    answerList.add(aMap);
                }
            }

            String primaryWeakTag = wrongTagMap.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("VOCAB");

            // 기존에 등록된 응시 기록이 있다면 하위 답안과 원장을 먼저 깔끔하게 삭제 후 재등록
            ExamAttempt existingAttempt = examMapper.findExamAttempt(examId, student.getStudentId());
            if (existingAttempt != null) {
                examMapper.deleteExamAnswersByAttemptId(existingAttempt.getAttemptId());
                examMapper.deleteExamAttemptByExamAndStudent(examId, student.getStudentId());
            }

            ExamAttempt attempt = new ExamAttempt();
            attempt.setExamId(examId);
            attempt.setStudentId(student.getStudentId());
            attempt.setAttemptNo(1);
            attempt.setTotalScore(totalScore);
            attempt.setMaxScore(maxScore);
            attempt.setCorrectCount(correctCount);
            attempt.setTotalQuestionCount(student.getAnswers() != null ? student.getAnswers().size() : 0);
            attempt.setPrimaryWeakTag(primaryWeakTag);

            examMapper.insertExamAttempt(attempt);

            if (!answerList.isEmpty()) {
                examMapper.batchInsertExamAnswers(attempt.getAttemptId(), answerList);
            }
        }
    }

    // 성적 분석 통계 반환 서비스
    @Transactional(readOnly = true)
    public Map<String, Object> getExamStats(Long examId, Long classId) {
        return examMapper.getExamStats(examId, classId);
    }

    // DB 제약 조건 에러 방지를 위해 UPDATE 후 실패(0건) 시 INSERT 수행
    @Transactional
    public void saveTeacherComments(List<Map<String, Object>> comments) {
        for (Map<String, Object> c : comments) {
            Long studentId = Long.valueOf(c.get("studentId").toString());
            String comment = c.get("comment").toString();

            // 1. 이미 작성된 리포트가 있으면 코멘트 내용만 최신으로 수정(UPDATE)
            int updatedRows = examMapper.updateTeacherComment(studentId, comment);

            // 2. 작성된 리포트가 아예 없다면 새로 생성(INSERT)
            if (updatedRows == 0) {
                examMapper.insertTeacherComment(studentId, comment);
            }
        }
    }
}