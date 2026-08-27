package com.example.EduPOP.service.exam;

import com.example.EduPOP.controller.exam.dto.ExamBulkGradeRequest;
import com.example.EduPOP.domain.exam.*;
import com.example.EduPOP.repository.exam.ExamMapper;
import com.example.EduPOP.repository.exam.ExamQuestionChoiceMapper;
import com.example.EduPOP.repository.exam.ExamQuestionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamServiceTest {

    @Mock
    private ExamMapper examMapper;

    @Mock
    private ExamQuestionMapper examQuestionMapper;

    @Mock
    private ExamQuestionChoiceMapper examQuestionChoiceMapper;

    private ExamService examService;

    @BeforeEach
    void setUp() {
        examService = new ExamService(examMapper, examQuestionMapper, examQuestionChoiceMapper);
    }

    @Test
    void updateExam_updatesOnlyExistingExamQuestionsAndChoices() {
        Exam savedExam = savedExam();
        ExamQuestion savedQuestion = savedQuestion(10L);
        ExamQuestionChoice savedChoice = savedChoice(100L, 10L);
        Exam requestedExam = requestedExam(List.of(requestedQuestion(10L, 100L)));

        when(examMapper.findById(1L)).thenReturn(savedExam);
        when(examQuestionMapper.findByExamId(1L)).thenReturn(List.of(savedQuestion));
        when(examQuestionChoiceMapper.findByQuestionId(10L)).thenReturn(List.of(savedChoice));

        examService.updateExam(1L, 7L, requestedExam);

        verify(examMapper).update(savedExam);
        verify(examQuestionMapper).update(savedQuestion);
        verify(examQuestionChoiceMapper).update(savedChoice);
        verify(examQuestionMapper, never()).insert(any());
        verify(examQuestionMapper, never()).delete(any());
        verify(examQuestionChoiceMapper, never()).insert(any());
        verify(examQuestionChoiceMapper, never()).deleteByQuestionId(any());
    }

    @Test
    void updateExam_rejectsAddedQuestionId() {
        Exam savedExam = savedExam();
        ExamQuestion savedQuestion = savedQuestion(10L);
        Exam requestedExam = requestedExam(List.of(
                requestedQuestion(10L, 100L),
                requestedQuestion(11L, 101L)
        ));

        when(examMapper.findById(1L)).thenReturn(savedExam);
        when(examQuestionMapper.findByExamId(1L)).thenReturn(List.of(savedQuestion));

        assertThrows(IllegalArgumentException.class,
                () -> examService.updateExam(1L, 7L, requestedExam));

        verify(examMapper, never()).update(any());
        verify(examQuestionMapper, never()).update(any());
        verify(examQuestionMapper, never()).insert(any());
    }

    @Test
    void saveBulkGrades_completesManualAttemptForStudentResults() {
        ExamBulkGradeRequest.AnswerPayload answer =
                new ExamBulkGradeRequest.AnswerPayload();
        answer.setQuestionId(10L);
        answer.setSubmittedAnswer("2");
        answer.setCorrectAnswer("2");
        answer.setScore(5);
        answer.setQuestionType("VOCAB");

        ExamBulkGradeRequest.StudentGradePayload student =
                new ExamBulkGradeRequest.StudentGradePayload();
        student.setStudentId(20L);
        student.setAnswers(List.of(answer));

        ExamBulkGradeRequest request = new ExamBulkGradeRequest();
        request.setExamId(1L);
        request.setStudentGrades(List.of(student));

        when(examMapper.findExamAttempt(1L, 20L))
                .thenReturn(null);
        when(examMapper.insertExamAttempt(any(ExamAttempt.class)))
                .thenAnswer(invocation -> {
                    ExamAttempt attempt = invocation.getArgument(0);
                    attempt.setAttemptId(99L);
                    return 1;
                });

        examService.saveBulkGrades(request);

        ArgumentCaptor<ExamAttempt> attemptCaptor =
                ArgumentCaptor.forClass(ExamAttempt.class);
        verify(examMapper).insertExamAttempt(attemptCaptor.capture());

        ExamAttempt savedAttempt = attemptCaptor.getValue();

        assertAll(
                () -> assertEquals(1L, savedAttempt.getExamId()),
                () -> assertEquals(20L, savedAttempt.getStudentId()),
                () -> assertEquals(1, savedAttempt.getAttemptNo()),
                () -> assertEquals("EXAM", savedAttempt.getAttemptType()),
                () -> assertEquals("MANUAL", savedAttempt.getEntryMethod()),
                () -> assertEquals("GRADED", savedAttempt.getStatus()),
                () -> assertEquals(
                        BigDecimal.valueOf(5),
                        savedAttempt.getTotalScore()
                ),
                () -> assertEquals(
                        BigDecimal.valueOf(5),
                        savedAttempt.getMaxScore()
                ),
                () -> assertNotNull(savedAttempt.getSubmittedAt()),
                () -> assertEquals(
                        savedAttempt.getSubmittedAt(),
                        savedAttempt.getGradedAt()
                )
        );

        verify(examMapper).batchInsertExamAnswers(
                eq(99L),
                anyList()
        );
    }

    private Exam savedExam() {
        Exam exam = new Exam();
        exam.setExamId(1L);
        exam.setTeacherId(7L);
        exam.setClassId(2L);
        exam.setTitle("기존 시험");
        exam.setExamType("MONTHLY");
        exam.setExamMode(ExamMode.PAPER);
        exam.setExamRound(1);
        exam.setStatus(ExamStatus.DRAFT);
        return exam;
    }

    private Exam requestedExam(List<ExamQuestion> questions) {
        Exam exam = new Exam();
        exam.setClassId(2L);
        exam.setTitle("수정한 시험");
        exam.setExamType("MONTHLY");
        exam.setExamMode(ExamMode.PAPER);
        exam.setStatus(ExamStatus.OPEN);
        exam.setQuestions(questions);
        return exam;
    }

    private ExamQuestion savedQuestion(Long questionId) {
        ExamQuestion question = new ExamQuestion();
        question.setQuestionId(questionId);
        question.setExamId(1L);
        question.setQuestionNumber(1);
        question.setQuestionType("MULTIPLE_CHOICE");
        question.setQuestionTypeTag("GRAMMAR");
        question.setQuestionText("기존 문제");
        question.setSortOrder(1);
        return question;
    }

    private ExamQuestion requestedQuestion(Long questionId, Long choiceId) {
        ExamQuestion question = new ExamQuestion();
        question.setQuestionId(questionId);
        question.setQuestionTypeTag("VOCABULARY");
        question.setQuestionText("수정한 문제");
        question.setScore(new BigDecimal("5.00"));
        question.setCorrectAnswer("1");
        question.setChoices(List.of(requestedChoice(choiceId)));
        return question;
    }

    private ExamQuestionChoice savedChoice(Long choiceId, Long questionId) {
        ExamQuestionChoice choice = new ExamQuestionChoice();
        choice.setChoiceId(choiceId);
        choice.setQuestionId(questionId);
        choice.setChoiceNumber(1);
        choice.setChoiceText("기존 선지");
        choice.setSortOrder(1);
        return choice;
    }

    private ExamQuestionChoice requestedChoice(Long choiceId) {
        ExamQuestionChoice choice = new ExamQuestionChoice();
        choice.setChoiceId(choiceId);
        choice.setChoiceText("수정한 선지");
        return choice;
    }
}