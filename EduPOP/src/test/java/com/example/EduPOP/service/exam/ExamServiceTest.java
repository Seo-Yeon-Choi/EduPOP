package com.example.EduPOP.service.exam;

import com.example.EduPOP.domain.exam.*;
import com.example.EduPOP.repository.exam.ExamMapper;
import com.example.EduPOP.repository.exam.ExamQuestionChoiceMapper;
import com.example.EduPOP.repository.exam.ExamQuestionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
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
        question.setLargeCategory("GRAMMAR");
        question.setSmallCategory("RELATIVE_CLAUSE");
        question.setQuestionText("기존 문제");
        question.setSortOrder(1);
        return question;
    }

    private ExamQuestion requestedQuestion(Long questionId, Long choiceId) {
        ExamQuestion question = new ExamQuestion();
        question.setQuestionId(questionId);
        question.setQuestionType("MULTIPLE_CHOICE");
        question.setLargeCategory("VOCABULARY");
        question.setSmallCategory("SYNONYM");
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
