package com.example.EduPOP.controller.exam;

import com.example.EduPOP.domain.exam.Exam;
import com.example.EduPOP.domain.exam.ExamQuestion;
import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.domain.user.UserRole;
import com.example.EduPOP.service.classroom.ClassService;
import com.example.EduPOP.service.exam.ExamQuestionParseService;
import com.example.EduPOP.service.exam.ExamService;
import com.example.EduPOP.service.exam.PdfTextExtractService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ExtendedModelMap;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExamControllerTest {

    @Mock
    private ExamService examService;

    @Mock
    private ClassService classService;

    @Mock
    private PdfTextExtractService pdfTextExtractService;

    @Mock
    private ExamQuestionParseService examQuestionParseService;

    private ExamController controller;

    @BeforeEach
    void setUp() {
        controller = new ExamController(
                examService,
                classService,
                pdfTextExtractService,
                examQuestionParseService
        );
    }

    @Test
    void listRedirectsWhenNoTeacherIsLoggedIn() {
        String view = controller.examList(new MockHttpSession(), new ExtendedModelMap());

        assertThat(view).isEqualTo("redirect:/LocalLogin");
        verifyNoInteractions(examService);
    }

    @Test
    void listRejectsAStudentSession() {
        MockHttpSession session = userSession(7L, UserRole.STUDENT);

        String view = controller.examList(session, new ExtendedModelMap());

        assertThat(view).isEqualTo("redirect:/LocalLogin");
        verifyNoInteractions(examService);
    }

    @Test
    void listLoadsOnlyTheLoggedInTeachersExams() {
        MockHttpSession session = userSession(20L, UserRole.TEACHER);
        List<Exam> exams = List.of(new Exam());
        when(examService.getExamListByTeacher(20L)).thenReturn(exams);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.examList(session, model);

        assertThat(view).isEqualTo("layout/exam/list");
        assertThat(model.get("exams")).isSameAs(exams);
        verify(examService).getExamListByTeacher(20L);
    }

    @Test
    void createPageLoadsOnlyAssignedClasses() {
        MockHttpSession session = userSession(20L, UserRole.TEACHER);
        when(classService.getClassesByTeacher(20L)).thenReturn(List.of());

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.createPage(session, model);

        assertThat(view).isEqualTo("layout/exam/create");
        assertThat(model).containsKey("classes");
        verify(classService).getClassesByTeacher(20L);
    }

    @Test
    void detailUsesTeacherOwnership() {
        MockHttpSession session = userSession(20L, UserRole.TEACHER);
        Exam exam = new Exam();
        when(examService.getExamDetailForTeacher(10L, 20L)).thenReturn(exam);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.detailPage(10L, session, model);

        assertThat(view).isEqualTo("layout/exam/detail");
        assertThat(model.get("exam")).isSameAs(exam);
        verify(examService).getExamDetailForTeacher(10L, 20L);
    }

    @Test
    void createOverwritesTeacherIdWithTheSessionUser() {
        MockHttpSession session = userSession(20L, UserRole.TEACHER);
        Exam exam = new Exam();
        exam.setTeacherId(999L);
        when(examService.createExam(exam)).thenReturn(10L);

        Long examId = controller.createExam(exam, session);

        assertThat(examId).isEqualTo(10L);
        assertThat(exam.getTeacherId()).isEqualTo(20L);
        verify(examService).createExam(exam);
    }

    @Test
    void createFailsWithoutATeacherSession() {
        assertThatThrownBy(() ->
                controller.createExam(new Exam(), new MockHttpSession()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("로그인이 필요합니다.");

        verifyNoInteractions(examService);
    }

    @Test
    void updateRejectsAnUnassignedClass() {
        MockHttpSession session = userSession(20L, UserRole.TEACHER);
        Exam changes = new Exam();
        changes.setClassId(300L);
        when(classService.getClassesByTeacher(20L)).thenReturn(List.of());

        assertThatThrownBy(() ->
                controller.updateExam(10L, changes, session))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("담당 반에만 시험을 등록할 수 있습니다.");

        verifyNoInteractions(examService);
    }

    @Test
    void wordPdfUsesTheWordQuestionParser() {
        MockMultipartFile file =
                new MockMultipartFile("file", "word.pdf", "application/pdf", new byte[]{1});
        List<ExamQuestion> questions = List.of(mock(ExamQuestion.class));

        when(pdfTextExtractService.extractText(file)).thenReturn("word text");
        when(examQuestionParseService.parseWordExam("word text")).thenReturn(questions);

        List<ExamQuestion> result = controller.parsePdf(file, "word");

        assertThat(result).isSameAs(questions);
        verify(examQuestionParseService).parseWordExam("word text");
    }

    @Test
    void nonWordPdfUsesTheNormalQuestionParser() {
        MockMultipartFile file =
                new MockMultipartFile("file", "monthly.pdf", "application/pdf", new byte[]{1});
        List<ExamQuestion> questions = List.of(mock(ExamQuestion.class));

        when(pdfTextExtractService.extractText(file)).thenReturn("normal text");
        when(examQuestionParseService.parseNormalExam("normal text")).thenReturn(questions);

        List<ExamQuestion> result = controller.parsePdf(file, "MONTHLY");

        assertThat(result).isSameAs(questions);
        verify(examQuestionParseService).parseNormalExam("normal text");
    }

    private MockHttpSession userSession(Long userId, UserRole role) {
        User user = new User();
        user.setUserId(userId);
        user.setRole(role);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginUser", user);
        return session;
    }
}
