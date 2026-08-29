package com.example.EduPOP.controller;

import com.example.EduPOP.domain.report.ParentReport;
import com.example.EduPOP.domain.report.StudentReport;
import com.example.EduPOP.service.report.ParentReportService;
import com.example.EduPOP.service.report.StudentReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.ExtendedModelMap;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportControllersTest {

    @Mock
    private StudentReportService studentReportService;

    @Mock
    private ParentReportService parentReportService;

    private StudentReportController studentController;
    private ParentReportController parentController;
    private SharedReportController sharedController;

    @BeforeEach
    void setUp() {
        studentController = new StudentReportController(studentReportService);
        parentController = new ParentReportController(parentReportService);
        sharedController = new SharedReportController(parentReportService);
    }

    @Test
    void monthlyStudentReportGenerationDelegatesTheRequestedPeriod() {
        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 31);
        StudentReportController.GenerateReportRequest request =
                new StudentReportController.GenerateReportRequest();
        request.setStudentId(7L);
        request.setPeriodStart(start);
        request.setPeriodEnd(end);

        StudentReport report = mock(StudentReport.class);
        when(studentReportService.createMonthlyReport(7L, start, end))
                .thenReturn(report);

        assertThat(studentController.generateReport(request)).isSameAs(report);
        verify(studentReportService).createMonthlyReport(7L, start, end);
    }

    @Test
    void studentReflectionUpdateReturnsTheSavedFilteredValue() {
        StudentReportController.ProudestRequest request =
                new StudentReportController.ProudestRequest();
        request.setProudestMoment("이번 달 목표 달성");

        StudentReport saved = mock(StudentReport.class);
        when(saved.getProudestMoment()).thenReturn("이번 달 목표 달성");
        when(studentReportService.getReport(9L)).thenReturn(saved);

        String result = studentController.updateProudestMoment(9L, request);

        assertThat(result).isEqualTo("이번 달 목표 달성");
        verify(studentReportService).updateProudestMoment(
                9L,
                "이번 달 목표 달성"
        );
    }

    @Test
    void moodAndScoreReturnTheSavedValues() {
        StudentReportController.MoodScoreRequest request =
                new StudentReportController.MoodScoreRequest();
        request.setMonthlyMood("뿌듯함");
        request.setSelfEffortScore(5);

        StudentReport saved = mock(StudentReport.class);
        when(saved.getMonthlyMood()).thenReturn("뿌듯함");
        when(saved.getSelfEffortScore()).thenReturn(5);
        when(studentReportService.getReport(9L)).thenReturn(saved);

        StudentReportController.MoodScoreRequest response =
                studentController.updateMoodAndScore(9L, request);

        assertThat(response.getMonthlyMood()).isEqualTo("뿌듯함");
        assertThat(response.getSelfEffortScore()).isEqualTo(5);
        verify(studentReportService).updateMoodAndScore(9L, "뿌듯함", 5);
    }

    @Test
    void parentReportFailsFastForAnUnknownReport() {
        when(parentReportService.getReport(99L)).thenReturn(null);

        assertThatThrownBy(() ->
                parentController.showParentReport(99L, new ExtendedModelMap()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("99");
    }

    @Test
    void parentReportLoadsTrendAndUsesAnEmptyRadarListWhenNoDataExists() {
        ParentReport report = mock(ParentReport.class);
        when(report.getStudentId()).thenReturn(7L);

        Map<String, List<?>> trend = Map.of(
                "chartLabels", List.of("8월"),
                "studentScores", List.of(90),
                "classScores", List.of(80)
        );

        when(parentReportService.getReport(9L)).thenReturn(report);
        when(parentReportService.getScoreTrend(7L)).thenReturn(trend);
        when(parentReportService.getRadarStats(report)).thenReturn(null);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = parentController.showParentReport(9L, model);

        assertThat(view).isEqualTo("parent");
        assertThat(model.get("report")).isSameAs(report);
        assertThat(model.get("chartLabels")).isEqualTo(List.of("8월"));
        assertThat(model.get("radarStats")).isEqualTo(List.of());
    }

    @Test
    void unlinkedSharedReportShowsDeviceVerification() {
        ParentReport report = mock(ParentReport.class);
        when(report.getStudentId()).thenReturn(7L);
        when(parentReportService.getReportByToken("report-token")).thenReturn(report);
        when(parentReportService.isValidDevice(7L, null)).thenReturn(false);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = sharedController.viewSharedReport(
                "report-token",
                null,
                model
        );

        assertThat(view).isEqualTo("verify");
        assertThat(model.get("token")).isEqualTo("report-token");
    }

    @Test
    void successfulClassAuthenticationRegistersADeviceCookie() {
        ParentReport report = mock(ParentReport.class);
        when(report.getStudentId()).thenReturn(7L);
        when(parentReportService.authenticateAndGetReportToken(
                2L,
                "학생",
                "1234"
        )).thenReturn("report-token");
        when(parentReportService.getReportByToken("report-token")).thenReturn(report);
        when(parentReportService.registerDeviceLink(7L)).thenReturn("device-token");

        MockHttpServletResponse response = new MockHttpServletResponse();
        String view = sharedController.verifyClassAuth(
                2L,
                "학생",
                "1234",
                response,
                new ExtendedModelMap()
        );

        assertThat(view).isEqualTo("redirect:/share/reports/report-token");
        assertThat(response.getCookie("EDUPOP_DEVICE")).isNotNull();
        assertThat(response.getCookie("EDUPOP_DEVICE").getValue())
                .isEqualTo("device-token");
        assertThat(response.getCookie("EDUPOP_DEVICE").getPath())
                .isEqualTo("/");
    }

    @Test
    void failedClassAuthenticationStaysOnTheAuthPage() {
        when(parentReportService.authenticateAndGetReportToken(
                2L,
                "학생",
                "0000"
        )).thenReturn(null);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = sharedController.verifyClassAuth(
                2L,
                "학생",
                "0000",
                new MockHttpServletResponse(),
                model
        );

        assertThat(view).isEqualTo("report-auth");
        assertThat(model.get("classId")).isEqualTo(2L);
        assertThat(model).containsKey("error");
    }
}
