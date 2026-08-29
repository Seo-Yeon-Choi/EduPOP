package com.example.EduPOP.controller.exam;

import com.example.EduPOP.controller.exam.dto.ExamBulkGradeRequest;
import com.example.EduPOP.controller.exam.dto.ExamCommentSaveRequest;
import com.example.EduPOP.controller.exam.dto.ExamCreateOrCopyRequest;
import com.example.EduPOP.controller.exam.dto.ExamDetailResponse;
import com.example.EduPOP.controller.exam.dto.ExamListResponse;
import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.service.classroom.ClassroomService;
import com.example.EduPOP.service.exam.ExamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExamGradingControllerTest {

    @Mock
    private ExamService examService;

    @Mock
    private ClassroomService classroomService;

    private ExamGradingController controller;

    @BeforeEach
    void setUp() {
        controller = new ExamGradingController(examService, classroomService);
    }

    @Test
    void listRedirectsWhenNoTeacherIsLoggedIn() {
        String view = controller.viewExamList(
                null,
                new MockHttpSession(),
                new ExtendedModelMap()
        );

        assertThat(view).isEqualTo("redirect:/login");
        verifyNoInteractions(examService, classroomService);
    }

    @Test
    void listFiltersExamsByTheSelectedClass() {
        MockHttpSession session = teacherSession(20L, 3L);
        ExamListResponse classOne =
                new ExamListResponse(10L, 1L, 1, "A", "1반", "2026-08-29", 10, 2);
        ExamListResponse classTwo =
                new ExamListResponse(11L, 2L, 1, "B", "2반", "2026-08-29", 10, 3);

        when(classroomService.findAllByAcademyId(3L, null)).thenReturn(List.of());
        when(examService.getAllExamList()).thenReturn(List.of(classOne, classTwo));
        when(examService.getExamTemplates(3L)).thenReturn(List.of());

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.viewExamList(2L, session, model);

        assertThat(view).isEqualTo("exam/exam-list");
        assertThat((List<?>) model.get("exams")).containsExactly(classTwo);
        assertThat(model.get("currentClassId")).isEqualTo(2L);
        verify(examService).getExamTemplates(3L);
    }

    @Test
    void omrUsesThePathExamIdBeforeTheQueryParameter() {
        MockHttpSession session = teacherSession(20L, 3L);
        ExamDetailResponse detail = new ExamDetailResponse();
        when(examService.getExamDetailForOmr(12L)).thenReturn(detail);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.viewOmrMatrix(11L, 12L, session, model);

        assertThat(view).isEqualTo("exam/omr-matrix");
        assertThat(model.get("exam")).isSameAs(detail);
        verify(examService).getExamDetailForOmr(12L);
    }

    @Test
    void omrRedirectsWhenNoExamIdWasProvided() {
        String view = controller.viewOmrMatrix(
                null,
                null,
                teacherSession(20L, 3L),
                new ExtendedModelMap()
        );

        assertThat(view).isEqualTo("redirect:/exam/list");
        verifyNoInteractions(examService);
    }

    @Test
    void createSheetReturnsUnauthorizedWithoutASession() {
        ResponseEntity<Long> response = controller.createExamSheet(
                new ExamCreateOrCopyRequest(),
                new MockHttpSession()
        );

        assertThat(response.getStatusCode().value()).isEqualTo(401);
        verifyNoInteractions(examService);
    }

    @Test
    void createSheetIgnoresClientOwnedAcademyAndTeacherIds() {
        MockHttpSession session = teacherSession(20L, 3L);
        ExamCreateOrCopyRequest request = new ExamCreateOrCopyRequest();
        request.setAcademyId(999L);
        request.setTeacherId(888L);
        request.setClassId(2L);
        when(examService.createExamSheet(request)).thenReturn(50L);

        ResponseEntity<Long> response = controller.createExamSheet(request, session);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isEqualTo(50L);
        assertThat(request.getAcademyId()).isEqualTo(3L);
        assertThat(request.getTeacherId()).isEqualTo(20L);
        verify(examService).createExamSheet(request);
    }

    @Test
    void bulkGradesUseTheLoggedInTeacherId() {
        MockHttpSession session = teacherSession(20L, 3L);
        ExamBulkGradeRequest request = new ExamBulkGradeRequest();
        request.setExamId(50L);

        ResponseEntity<Map<String, Object>> response =
                controller.saveBulkGrades(request, session);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody())
                .containsEntry("success", true)
                .containsEntry("message", "채점 결과가 성공적으로 저장되었습니다.");
        verify(examService).saveBulkGrades(request, 20L);
    }

    @Test
    void statsAreReturnedFromTheService() {
        Map<String, Object> stats = Map.of("average", 87.5);
        when(examService.getExamStats(50L, 2L)).thenReturn(stats);

        ResponseEntity<Map<String, Object>> response =
                controller.getExamStats(50L, 2L, teacherSession(20L, 3L));

        assertThat(response.getBody()).isSameAs(stats);
        verify(examService).getExamStats(50L, 2L);
    }

    @Test
    void emptyTeacherCommentsDoNotCallTheService() {
        ExamCommentSaveRequest request = new ExamCommentSaveRequest();
        request.setExamId(50L);
        request.setComments(List.of());

        ResponseEntity<Map<String, Object>> response =
                controller.saveTeacherComments(request, teacherSession(20L, 3L));

        assertThat(response.getBody()).containsEntry("success", true);
        verify(examService, never()).saveTeacherComments(
                eq(50L),
                eq(20L),
                org.mockito.ArgumentMatchers.anyList()
        );
    }

    @Test
    void teacherCommentsAreSavedWithExamAndTeacherOwnership() {
        ExamCommentSaveRequest request = new ExamCommentSaveRequest();
        request.setExamId(50L);
        List<ExamCommentSaveRequest.StudentCommentPayload> comments =
                new ArrayList<>();
        comments.add(new ExamCommentSaveRequest.StudentCommentPayload());
        request.setComments(comments);

        controller.saveTeacherComments(request, teacherSession(20L, 3L));

        verify(examService).saveTeacherComments(
                eq(50L),
                eq(20L),
                same(comments)
        );
    }

    private MockHttpSession teacherSession(Long userId, Long academyId) {
        User teacher = new User();
        teacher.setUserId(userId);
        teacher.setAcademyId(academyId);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginUser", teacher);
        return session;
    }
}
