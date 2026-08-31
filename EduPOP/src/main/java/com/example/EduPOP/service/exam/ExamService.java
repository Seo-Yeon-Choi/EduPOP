package com.example.EduPOP.service.exam;

import com.example.EduPOP.controller.exam.dto.*;
import com.example.EduPOP.domain.exam.*;
import com.example.EduPOP.repository.exam.ExamMapper;
import com.example.EduPOP.repository.exam.ExamQuestionChoiceMapper;
import com.example.EduPOP.repository.exam.ExamQuestionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamMapper examMapper;
    private final ExamQuestionMapper examQuestionMapper;
    private final ExamQuestionChoiceMapper examQuestionChoiceMapper;

    public List<Exam> getExamList() {
        return examMapper.findAll();
    }

    public List<Exam> getExamListByTeacher(Long teacherId) {
        return examMapper.findByTeacherId(teacherId);
    }

    public Exam getExamDetailForTeacher(Long examId, Long teacherId) {
        Exam exam = findOwnedExam(examId, teacherId);
        List<ExamQuestion> questions = examQuestionMapper.findByExamId(examId);

        for (ExamQuestion question : questions) {
            question.setChoices(
                    examQuestionChoiceMapper.findByQuestionId(question.getQuestionId())
            );
        }

        exam.setQuestions(questions);
        return exam;
    }

    @Transactional
    public Long createExam(Exam exam) {
        if (exam.getStatus() == null) {
            exam.setStatus(ExamStatus.DRAFT);
        }

        if (exam.getExamRound() == null) {
            exam.setExamRound(1);
        }

        if (exam.getExamMode() == null) {
            exam.setExamMode(ExamMode.PAPER);
        }

        examMapper.insert(exam);

        for (ExamQuestion question : exam.getQuestions()) {
            question.setExamId(exam.getExamId());

            if (question.getSortOrder() == null) {
                question.setSortOrder(question.getQuestionNumber());
            }

            examQuestionMapper.insert(question);

            if ("MULTIPLE_CHOICE".equalsIgnoreCase(question.getQuestionType())
                    && question.getChoices() != null) {
                int number = 1;

                for (ExamQuestionChoice choice : question.getChoices()) {
                    if (isBlank(choice.getChoiceText())) {
                        continue;
                    }

                    choice.setQuestionId(question.getQuestionId());
                    choice.setChoiceNumber(number);
                    choice.setSortOrder(number);
                    examQuestionChoiceMapper.insert(choice);
                    number++;
                }
            }
        }

        return exam.getExamId();
    }

    @Transactional
    public void updateExam(Long examId, Long teacherId, Exam requestedExam) {
        Exam savedExam = findOwnedExam(examId, teacherId);
        validateExamInfo(requestedExam);

        List<ExamQuestion> savedQuestions = examQuestionMapper.findByExamId(examId);
        Map<Long, ExamQuestion> requestedQuestions = indexQuestions(
                requestedExam.getQuestions()
        );
        Set<Long> savedQuestionIds = new HashSet<>();

        for (ExamQuestion savedQuestion : savedQuestions) {
            savedQuestionIds.add(savedQuestion.getQuestionId());
        }

        if (!savedQuestionIds.equals(requestedQuestions.keySet())) {
            throw new IllegalArgumentException(
                    "기존 문항만 수정할 수 있습니다. 문항 추가 또는 삭제는 지원하지 않습니다."
            );
        }

        savedExam.setClassId(requestedExam.getClassId());
        savedExam.setTitle(requestedExam.getTitle().trim());
        savedExam.setExamType(requestedExam.getExamType());
        savedExam.setExamMode(requestedExam.getExamMode());
        savedExam.setStatus(requestedExam.getStatus());
        savedExam.setExamDate(requestedExam.getExamDate());
        examMapper.update(savedExam);

        for (ExamQuestion savedQuestion : savedQuestions) {
            ExamQuestion requestedQuestion = requestedQuestions.get(
                    savedQuestion.getQuestionId()
            );

            validateQuestion(requestedQuestion);
            savedQuestion.setLargeCategory(
                    trimToNull(requestedQuestion.getLargeCategory())
            );

            savedQuestion.setSmallCategory(
                    trimToNull(requestedQuestion.getSmallCategory())
            );
            savedQuestion.setScore(requestedQuestion.getScore());
            savedQuestion.setCorrectAnswer(
                    trimToNull(requestedQuestion.getCorrectAnswer())
            );
            savedQuestion.setQuestionText(
                    requestedQuestion.getQuestionText().trim()
            );
            savedQuestion.setPassage(
                    trimToNull(requestedQuestion.getPassage())
            );

            examQuestionMapper.update(savedQuestion);

            updateExistingChoices(
                    savedQuestion.getQuestionId(),
                    requestedQuestion.getChoices()
            );
        }
    }

    @Transactional
    public void addQuestion(Long examId, ExamQuestion question) {
        question.setExamId(examId);

        if (question.getSortOrder() == null) {
            question.setSortOrder(question.getQuestionNumber());
        }

        examQuestionMapper.insert(question);

        if ("MULTIPLE_CHOICE".equalsIgnoreCase(question.getQuestionType())
                && question.getChoices() != null) {
            int number = 1;

            for (ExamQuestionChoice choice : question.getChoices()) {
                if (isBlank(choice.getChoiceText())) {
                    continue;
                }

                choice.setQuestionId(question.getQuestionId());
                choice.setChoiceNumber(number);
                choice.setSortOrder(number);
                examQuestionChoiceMapper.insert(choice);
                number++;
            }
        }
    }

    @Transactional(readOnly = true)
    public List<ExamTemplateResponse> getExamTemplates(Long academyId) {
        return examMapper.findTemplatesByAcademyId(academyId);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createExamSheet(ExamCreateOrCopyRequest request) {
        if (request.getAcademyId() == null) {
            request.setAcademyId(1L);
        }

        if (request.getExamRound() == null) {
            request.setExamRound(1);
        }

        // 기존 MONTH 값은 표준 월말평가 코드인 MONTHLY로 통일
        String examType = request.getExamType();

        if (examType == null || examType.isBlank()) {
            request.setExamType("OTHER");
        } else {
            String normalizedExamType = examType.trim().toUpperCase();

            if ("MONTH".equals(normalizedExamType)
                    || "MONTHLY".equals(normalizedExamType)
                    || normalizedExamType.contains("월말")) {
                request.setExamType("MONTHLY");
            } else if ("WORD".equals(normalizedExamType)
                    || normalizedExamType.contains("단어")) {
                request.setExamType("WORD");
            } else {
                request.setExamType(normalizedExamType);
            }
        }

        examMapper.insertExam(request);

        Long newExamId = request.getGeneratedExamId();

        if (request.getTemplateExamId() != null) {
            examMapper.copyQuestionsFromTemplate(
                    newExamId,
                    request.getTemplateExamId()
            );
        } else if (request.getCustomQuestions() != null
                && !request.getCustomQuestions().isEmpty()) {
            examMapper.batchInsertCustomQuestions(
                    newExamId,
                    request.getCustomQuestions()
            );
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
            throw new IllegalArgumentException(
                    "존재하지 않는 시험지입니다. ID: " + examId
            );
        }

        detail.setQuestions(examMapper.findQuestionsByExamId(examId));

        if (detail.getClassId() != null) {
            detail.setStudents(
                    examMapper.findStudentsByExamAndClassId(
                            examId,
                            detail.getClassId()
                    )
            );
        }

        return detail;
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveBulkGrades(
            ExamBulkGradeRequest request,
            Long teacherId
    ) {
        Long examId = request.getExamId();

        log.info("시험 ID [{}] 일괄/개별 채점 저장 로직 실행", examId);

        if (request.getStudentGrades() == null
                || request.getStudentGrades().isEmpty()) {
            return;
        }

        for (ExamBulkGradeRequest.StudentGradePayload student
                : request.getStudentGrades()) {
            BigDecimal totalScore = BigDecimal.ZERO;
            BigDecimal maxScore = BigDecimal.ZERO;
            int correctCount = 0;

            Map<String, Integer> wrongTagMap = new HashMap<>();
            List<Map<String, Object>> answerList = new ArrayList<>();

            if (student.getAnswers() != null) {
                for (ExamBulkGradeRequest.AnswerPayload ans
                        : student.getAnswers()) {
                    BigDecimal score = ans.getScore() != null
                            ? BigDecimal.valueOf(ans.getScore())
                            : BigDecimal.valueOf(5);

                    maxScore = maxScore.add(score);

                    String submitted = ans.getSubmittedAnswer() != null
                            ? ans.getSubmittedAnswer().trim()
                            : "";

                    String correct = ans.getCorrectAnswer() != null
                            ? ans.getCorrectAnswer().trim()
                            : "";

                    boolean isCorrect = !submitted.isEmpty()
                            && submitted.equalsIgnoreCase(correct);

                    BigDecimal earned = isCorrect
                            ? score
                            : BigDecimal.ZERO;

                    if (isCorrect) {
                        correctCount++;
                        totalScore = totalScore.add(earned);
                    } else {
                        String tag = ans.getQuestionType() != null
                                && !ans.getQuestionType().isEmpty()
                                ? ans.getQuestionType()
                                : "VOCAB";

                        wrongTagMap.put(
                                tag,
                                wrongTagMap.getOrDefault(tag, 0) + 1
                        );
                    }

                    Map<String, Object> answerMap = new HashMap<>();
                    answerMap.put("questionId", ans.getQuestionId());
                    answerMap.put("studentAnswer", submitted);
                    answerMap.put("isCorrect", isCorrect ? 1 : 0);
                    answerMap.put("earnedScore", earned);
                    answerList.add(answerMap);
                }
            }

            String primaryWeakTag = wrongTagMap.entrySet()
                    .stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("VOCAB");

            ExamAttempt existingAttempt = examMapper.findExamAttempt(
                    examId,
                    student.getStudentId()
            );

            if (existingAttempt != null) {
                examMapper.deleteExamAnswersByAttemptId(
                        existingAttempt.getAttemptId()
                );
            }

            ExamAttempt attempt = new ExamAttempt();
            attempt.setExamId(examId);
            attempt.setStudentId(student.getStudentId());
            attempt.setAttemptNo(1);
            attempt.setTotalScore(totalScore);
            attempt.setMaxScore(maxScore);
            attempt.setCorrectCount(correctCount);
            attempt.setTotalQuestionCount(
                    student.getAnswers() != null
                            ? student.getAnswers().size()
                            : 0
            );
            attempt.setPrimaryWeakTag(primaryWeakTag);

            if (existingAttempt == null) {
                examMapper.insertExamAttempt(attempt);
            } else {
                attempt.setAttemptId(existingAttempt.getAttemptId());

                int updatedRows =
                        examMapper.updateExamAttemptScore(attempt);

                if (updatedRows != 1) {
                    throw new IllegalStateException(
                            "기존 시험 채점 결과 수정에 실패했습니다."
                    );
                }
            }

            if (!answerList.isEmpty()) {
                examMapper.batchInsertExamAnswers(
                        attempt.getAttemptId(),
                        answerList
                );
            }

            // 점수 저장 요청에 코멘트가 함께 왔다면 같은 시험 월의 학부모 리포트에 저장
            if (student.getTeacherComment() != null) {
                saveTeacherComment(
                        examId,
                        student.getStudentId(),
                        teacherId,
                        student.getTeacherComment()
                );
            }
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getExamStats(Long examId, Long classId) {
        return examMapper.getExamStats(examId, classId);
    }

    @Transactional
    public void saveTeacherComments(
            Long examId,
            Long teacherId,
            List<ExamCommentSaveRequest.StudentCommentPayload> comments
    ) {

        if (examId == null || teacherId == null || comments == null) {
            throw new IllegalArgumentException("시험·교사·코멘트 정보가 필요합니다.");
        }

        for (ExamCommentSaveRequest.StudentCommentPayload commentData : comments) {
            if (commentData == null || commentData.getStudentId() == null) {
                continue;
            }

            saveTeacherComment(
                    examId,
                    commentData.getStudentId(),
                    teacherId,
                    commentData.getComment()
            );
        }
    }

    // 해당 시험이 시행된 월의 parent_reports 한 건에 코멘트를 저장
    private void saveTeacherComment(
            Long examId,
            Long studentId,
            Long teacherId,
            String comment
    ) {

        String safeComment = comment != null ? comment.trim() : "";

        int updatedRows = examMapper.updateTeacherComment(
                examId,
                studentId,
                teacherId,
                safeComment
        );

        if (updatedRows == 0) {
            int insertedRows = examMapper.insertTeacherComment(
                    examId,
                    studentId,
                    teacherId,
                    safeComment
            );

            if (insertedRows != 1) {
                throw new IllegalStateException("학부모 리포트 코멘트 저장에 실패했습니다.");
            }
        }
    }

    private Exam findOwnedExam(Long examId, Long teacherId) {
        Exam exam = examMapper.findById(examId);

        if (exam == null) {
            throw new IllegalArgumentException("존재하지 않는 시험입니다.");
        }

        if (!teacherId.equals(exam.getTeacherId())) {
            throw new IllegalArgumentException(
                    "해당 시험을 조회하거나 수정할 권한이 없습니다."
            );
        }

        return exam;
    }

    private Map<Long, ExamQuestion> indexQuestions(
            List<ExamQuestion> questions
    ) {
        if (questions == null) {
            throw new IllegalArgumentException("문항 정보가 필요합니다.");
        }

        Map<Long, ExamQuestion> indexedQuestions = new HashMap<>();

        for (ExamQuestion question : questions) {
            if (question.getQuestionId() == null
                    || indexedQuestions.put(
                    question.getQuestionId(),
                    question
            ) != null) {
                throw new IllegalArgumentException(
                        "유효하지 않은 문항 정보입니다."
                );
            }
        }

        return indexedQuestions;
    }

    private void updateExistingChoices(
            Long questionId,
            List<ExamQuestionChoice> requestedChoices
    ) {
        List<ExamQuestionChoice> savedChoices =
                examQuestionChoiceMapper.findByQuestionId(questionId);

        Map<Long, ExamQuestionChoice> requestedChoiceMap = new HashMap<>();

        if (requestedChoices != null) {
            for (ExamQuestionChoice choice : requestedChoices) {
                if (choice.getChoiceId() == null
                        || requestedChoiceMap.put(
                        choice.getChoiceId(),
                        choice
                ) != null) {
                    throw new IllegalArgumentException(
                            "유효하지 않은 선지 정보입니다."
                    );
                }
            }
        }

        Set<Long> savedChoiceIds = new HashSet<>();

        for (ExamQuestionChoice choice : savedChoices) {
            savedChoiceIds.add(choice.getChoiceId());
        }

        if (!savedChoiceIds.equals(requestedChoiceMap.keySet())) {
            throw new IllegalArgumentException(
                    "기존 선지만 수정할 수 있습니다. 선지 추가 또는 삭제는 지원하지 않습니다."
            );
        }

        for (ExamQuestionChoice savedChoice : savedChoices) {
            ExamQuestionChoice requestedChoice =
                    requestedChoiceMap.get(savedChoice.getChoiceId());

            if (isBlank(requestedChoice.getChoiceText())) {
                throw new IllegalArgumentException(
                        "선지 내용은 비워 둘 수 없습니다."
                );
            }

            savedChoice.setChoiceText(
                    requestedChoice.getChoiceText().trim()
            );

            examQuestionChoiceMapper.update(savedChoice);
        }
    }

    private void validateExamInfo(Exam exam) {
        if (exam.getClassId() == null
                || isBlank(exam.getTitle())
                || exam.getExamType() == null
                || exam.getExamMode() == null
                || exam.getStatus() == null) {
            throw new IllegalArgumentException(
                    "시험 기본 정보를 모두 입력해주세요."
            );
        }
    }

    private void validateQuestion(
            ExamQuestion question
    ) {

        if (question == null
                || isBlank(question.getQuestionText())
                || isBlank(question.getLargeCategory())
                || isBlank(question.getSmallCategory())) {

            throw new IllegalArgumentException(
                    "문제 내용과 대분류/소분류를 모두 입력해주세요."
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    // 시험 리스트 화면 전용 서비스 메서드
    public List<ExamListResponse> getExamListResponseByTeacher(Long teacherId, Long classId) {
        return examMapper.findExamListByTeacherId(teacherId, classId);
    }
}
