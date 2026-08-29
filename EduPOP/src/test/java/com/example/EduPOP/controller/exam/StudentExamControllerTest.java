package com.example.EduPOP.controller.exam;

import com.example.EduPOP.domain.common.Paging;
import com.example.EduPOP.domain.exam.DailyReviewAttempt;
import com.example.EduPOP.domain.exam.DailyReviewSubmission;
import com.example.EduPOP.domain.exam.Exam;
import com.example.EduPOP.domain.exam.ExamAttempt;
import com.example.EduPOP.domain.exam.ExamQuestion;
import com.example.EduPOP.domain.exam.ExamSubmission;
import com.example.EduPOP.domain.exam.StudentExam;
import com.example.EduPOP.domain.exam.StudentExamResult;
import com.example.EduPOP.domain.exam.StudentGrowthSummary;
import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.service.exam.StudentExamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentExamControllerTest {

    @Mock
    private StudentExamService studentExamService;

    private StudentExamController controller;

    @BeforeEach
    void setUp() {
        controller = new StudentExamController(studentExamService);
    }

    @Test
    void examListRedirectsWhenTheStudentIsNotLoggedIn() {
        String view = controller.examList(1, null, new MockHttpSession(), new ExtendedModelMap());

        assertThat(view).isEqualTo("redirect:/LocalLogin");
        verifyNoInteractions(studentExamService);
    }

    @Test
    void examListLoadsPagingExamsAndTodayReviewCount() {
        MockHttpSession session = studentSession(7L);
        Paging paging = mock(Paging.class);
        List<StudentExam> exams = List.of();

        when(studentExamService.getStudentExamPaging(7L, null, 1)).thenReturn(paging);
        when(studentExamService.getStudentExams(7L, null, paging)).thenReturn(exams);
        when(studentExamService.getTodayReviewCount(7L)).thenReturn(3);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.examList(1, null, session, model);

        assertThat(view).isEqualTo("student/exam/list");
        assertThat(model.get("exams")).isSameAs(exams);
        assertThat(model.get("paging")).isSameAs(paging);
        assertThat(model.get("todayReviewCount")).isEqualTo(3);
    }

    @Test
    void takeExamAllowsOnlyWordExams() {
        MockHttpSession session = studentSession(7L);
        Exam nonWordExam = new Exam();
        nonWordExam.setExamType("MONTHLY");
        when(studentExamService.getExam(10L)).thenReturn(nonWordExam);

        assertThatThrownBy(() ->
                controller.takeExam(10L, session, new ExtendedModelMap()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("단어 시험만 응시할 수 있습니다.");
    }

    @Test
    void takeExamStartsAttemptAndLoadsQuestions() {
        MockHttpSession session = studentSession(7L);
        Exam exam = new Exam();
        exam.setExamType("WORD");
        ExamAttempt attempt = mock(ExamAttempt.class);
        List<ExamQuestion> questions = List.of(mock(ExamQuestion.class));

        when(studentExamService.getExam(10L)).thenReturn(exam);
        when(studentExamService.startWordExam(10L, 7L)).thenReturn(attempt);
        when(studentExamService.getWordExamQuestions(10L)).thenReturn(questions);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.takeExam(10L, session, model);

        assertThat(view).isEqualTo("student/exam/take");
        assertThat(model.get("attempt")).isSameAs(attempt);
        assertThat(model.get("questions")).isSameAs(questions);
        assertThat(model.get("mode")).isEqualTo("EXAM");
    }

    @Test
    void reviewRejectsWordExams() {
        MockHttpSession session = studentSession(7L);
        Exam wordExam = new Exam();
        wordExam.setExamType("WORD");
        when(studentExamService.getExam(10L)).thenReturn(wordExam);

        assertThatThrownBy(() ->
                controller.reviewExam(10L, session, new ExtendedModelMap()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("단어 시험은 복습 방식이 아닙니다.");
    }

    @Test
    void reviewStartsAttemptAndLoadsOnlyReviewQuestions() {
        MockHttpSession session = studentSession(7L);
        Exam exam = new Exam();
        exam.setExamType("MONTHLY");
        ExamAttempt attempt = mock(ExamAttempt.class);
        List<ExamQuestion> questions = List.of(mock(ExamQuestion.class));

        when(studentExamService.getExam(10L)).thenReturn(exam);
        when(studentExamService.startReview(10L, 7L)).thenReturn(attempt);
        when(studentExamService.getReviewQuestions(10L, 7L)).thenReturn(questions);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.reviewExam(10L, session, model);

        assertThat(view).isEqualTo("student/exam/take");
        assertThat(model.get("mode")).isEqualTo("REVIEW");
        assertThat(model.get("questions")).isSameAs(questions);
    }

    @Test
    void submitReturnsLoginFailureWithoutCallingTheService() {
        Map<String, Object> response = controller.submit(
                new ExamSubmission(),
                new MockHttpSession()
        );

        assertThat(response)
                .containsEntry("success", false)
                .containsEntry("message", "로그인이 필요합니다.");
        verifyNoInteractions(studentExamService);
    }

    @Test
    void submitUsesTheLoggedInStudentId() {
        ExamSubmission submission = new ExamSubmission();
        when(studentExamService.submitExam(7L, submission)).thenReturn(41L);

        Map<String, Object> response = controller.submit(submission, studentSession(7L));

        assertThat(response)
                .containsEntry("success", true)
                .containsEntry("attemptId", 41L);
        verify(studentExamService).submitExam(7L, submission);
    }

    @Test
    void resultPageUsesStudentOwnershipWhenLoadingTheAttempt() {
        MockHttpSession session = studentSession(7L);
        ExamAttempt attempt = mock(ExamAttempt.class);
        Exam exam = new Exam();
        when(attempt.getExamId()).thenReturn(10L);
        when(studentExamService.getResult(41L, 7L)).thenReturn(attempt);
        when(studentExamService.getExam(10L)).thenReturn(exam);
        when(studentExamService.getResultAnswers(41L)).thenReturn(List.of());

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.result(41L, "list", session, model);

        assertThat(view).isEqualTo("student/exam/result");
        assertThat(model.get("from")).isEqualTo("list");
        verify(studentExamService).getResult(41L, 7L);
    }

    @Test
    void resultListLoadsGrowthSummaryAndPaging() {
        MockHttpSession session = studentSession(7L);
        Paging paging = mock(Paging.class);
        List<StudentExamResult> results = List.of();
        StudentGrowthSummary summary = mock(StudentGrowthSummary.class);

        when(studentExamService.getStudentResultPaging(7L, null, 1)).thenReturn(paging);
        when(studentExamService.getStudentExamResults(7L, null, paging)).thenReturn(results);
        when(studentExamService.getStudentGrowthSummary(7L)).thenReturn(summary);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.resultList(1, null, session, model);

        assertThat(view).isEqualTo("layout/exam/result-list");
        assertThat(model.get("results")).isSameAs(results);
        assertThat(model.get("growthSummary")).isSameAs(summary);
        assertThat(model.get("paging")).isSameAs(paging);
    }

    @Test
    void todayReviewStartsDailyAttempt() {
        MockHttpSession session = studentSession(7L);
        DailyReviewAttempt attempt = mock(DailyReviewAttempt.class);
        List<ExamQuestion> questions = List.of(mock(ExamQuestion.class));

        when(studentExamService.startTodayReview(7L)).thenReturn(attempt);
        when(studentExamService.getTodayReviewQuestions(7L)).thenReturn(questions);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.todayReview(session, model);

        assertThat(view).isEqualTo("student/exam/today-review");
        assertThat(model.get("attempt")).isSameAs(attempt);
        assertThat(model.get("questions")).isSameAs(questions);
        assertThat(model.get("mode")).isEqualTo("DAILY_REVIEW");
    }

    @Test
    void todayReviewSubmissionUsesTheLoggedInStudentId() {
        DailyReviewSubmission submission = new DailyReviewSubmission();
        when(studentExamService.submitTodayReview(7L, submission)).thenReturn(51L);

        Map<String, Object> response =
                controller.submitTodayReview(submission, studentSession(7L));

        assertThat(response)
                .containsEntry("success", true)
                .containsEntry("attemptId", 51L);
        verify(studentExamService).submitTodayReview(7L, submission);
    }

    private MockHttpSession studentSession(Long userId) {
        User student = new User();
        student.setUserId(userId);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginUser", student);
        return session;
    }
}
