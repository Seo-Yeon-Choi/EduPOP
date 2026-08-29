package com.example.EduPOP.controller.classroom;

import com.example.EduPOP.domain.classroom.Classroom;
import com.example.EduPOP.service.classroom.ClassroomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ClassroomControllerTest {

    @Mock
    private ClassroomService classroomService;

    private ClassroomController controller;

    @BeforeEach
    void setUp() {
        controller = new ClassroomController(classroomService);
    }

    @Test
    void singleStatusChangeDelegatesTheEnumValue() {
        ResponseEntity<String> response = controller.updateStatus(
                10L,
                Map.of("status", "CLOSED")
        );

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(classroomService).updateStatus(
                10L,
                Classroom.ClassStatus.CLOSED
        );
    }

    @Test
    void invalidStatusReturnsServerErrorInsteadOfPartiallyUpdating() {
        ResponseEntity<String> response = controller.updateStatus(
                10L,
                Map.of("status", "UNKNOWN")
        );

        assertThat(response.getStatusCode().value()).isEqualTo(500);
    }

    @Test
    void bulkStatusChangeConvertsJsonNumbersToLongIds() {
        ResponseEntity<String> response = controller.updateStatusesBulk(
                Map.of(
                        "classIds", List.of(10, "11", 12L),
                        "status", "ACTIVE"
                )
        );

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(classroomService).updateStatusesBulk(
                List.of(10L, 11L, 12L),
                Classroom.ClassStatus.ACTIVE
        );
    }

    @Test
    void addingAStudentRequiresAStudentId() {
        ResponseEntity<?> response = controller.addStudent(10L, Map.of());

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isEqualTo("학생 식별자가 누락되었습니다.");
    }

    @Test
    void addingAStudentReturnsDomainValidationMessages() {
        doThrow(new IllegalStateException("이미 배정된 학생입니다."))
                .when(classroomService)
                .addStudent(10L, 7L);

        ResponseEntity<?> response = controller.addStudent(
                10L,
                Map.of("studentId", 7L)
        );

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isEqualTo("이미 배정된 학생입니다.");
    }

    @Test
    void teacherAndStudentRemovalDelegateExactIds() {
        assertThat(controller.removeTeacher(10L, 20L).getBody())
                .isEqualTo("OK");
        assertThat(controller.removeStudent(10L, 7L).getBody())
                .isEqualTo("STUDENT_REMOVED");

        verify(classroomService).removeTeacher(10L, 20L);
        verify(classroomService).removeStudent(10L, 7L);
    }

    @Test
    void studentSyncConvertsTheRequestIds() {
        ResponseEntity<String> response = controller.syncStudents(
                10L,
                Map.of("studentIds", List.of(7, "8", 9L))
        );

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(classroomService).syncStudents(10L, List.of(7L, 8L, 9L));
    }

    @Test
    void missingStudentSyncListMeansRemoveAllStudents() {
        ResponseEntity<String> response = controller.syncStudents(
                10L,
                Map.of()
        );

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(classroomService).syncStudents(10L, List.of());
    }
}
