package com.example.EduPOP.controller.analytics;

import com.example.EduPOP.config.SessionConst;
import com.example.EduPOP.controller.analytics.dto.StudentTrendResponse.SubCategoryStatDto;
import com.example.EduPOP.controller.analytics.dto.ClassWarningResponse;
import com.example.EduPOP.controller.analytics.dto.StudentTrendResponse;
import com.example.EduPOP.controller.classroom.dto.ClassroomDetailResponse;
import com.example.EduPOP.controller.classroom.dto.ClassroomListResponse;
import com.example.EduPOP.domain.common.Paging;
import com.example.EduPOP.domain.exam.StudentExamResult;
import com.example.EduPOP.domain.exam.StudentGrowthSummary;
import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.domain.user.UserRole;
import com.example.EduPOP.repository.analytics.AnalyticsMapper;
import com.example.EduPOP.service.analytics.AnalyticsService;
import com.example.EduPOP.service.analytics.ClassWarningService;
import com.example.EduPOP.service.classroom.ClassroomService;
import com.example.EduPOP.service.exam.StudentExamService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final ClassWarningService classWarningService;
    private final AnalyticsMapper analyticsMapper;
    private final ClassroomService classroomService;
    private final StudentExamService studentExamService;

    @GetMapping("/student-trend")
    public String viewStudentTrend(@RequestParam(name = "studentId", required = false) Long studentId,
                                   @RequestParam(name = "examId", required = false) Long examId,
                                   Model model) {
        if (studentId == null) studentId = 7L;

        log.info("학생 ID [{}] 성적 분석 리포트 조회 (연결된 시험 ID: [{}])", studentId, examId);
        StudentTrendResponse trendData = analyticsService.getStudentTrendData(studentId);

        model.addAttribute("report", trendData);
        model.addAttribute("currentExamId", examId != null ? examId : 10L);

        return "analytics/student-trend";
    }

    @GetMapping("/class-trend")
    public String viewClassTrend(@RequestParam(name = "examId", required = false) Long examId,
                                 @RequestParam(name = "classId", required = false) Long classId,
                                 @RequestParam(name = "academyId", required = false) Long academyId,
                                 Model model) {

        Long targetAcademyId = (academyId != null) ? academyId : 1L;
        List<ClassroomListResponse> teacherClasses = classroomService.findAllByAcademyId(targetAcademyId, "ACTIVE");

        model.addAttribute("teacherClasses", teacherClasses);
        model.addAttribute("examId", examId != null ? examId : 1L);
        model.addAttribute("classId", classId != null ? classId : 1L);

        return "analytics/class-trend";
    }

    @GetMapping("/api/class/{classId}/warning-signal")
    @ResponseBody
    public ResponseEntity<ClassWarningResponse> getClassWarningSignal(@PathVariable("classId") Long classId) {
        String className = "담당 반";

        List<ClassWarningResponse.ExamComparisonDto> examComparisons = analyticsMapper.findExamComparisonsByClassId(classId);
        List<SubCategoryStatDto> classWorst = analyticsMapper.findClassCategoryStats(classId, true);
        List<SubCategoryStatDto> classTop = analyticsMapper.findClassCategoryStats(classId, false);

        List<ClassWarningResponse.StudentVulnerableDto> students = analyticsMapper.findVulnerableStudentsByClassId(classId);
        for (ClassWarningResponse.StudentVulnerableDto student : students) {
            student.setTopWeakWords(analyticsMapper.findTopWeakWordsByStudentId(student.getStudentId()));
            student.setTopWeakTypes(analyticsMapper.findTopWeakTypesByStudentId(student.getStudentId()));
        }

        ClassWarningResponse response = classWarningService.evaluateClassWarning(
                classId, className, students, examComparisons, classWorst, classTop
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 학생 관리 메인 페이지 렌더링 (중복 제거 완료)
     */
    @GetMapping("/teacher/students")
    public String studentManagementPage(HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);
        if (loginUser == null || loginUser.getRole() != UserRole.TEACHER) {
            return "redirect:/LocalLogin";
        }

        // 로그인한 선생님 학원의 활성화된 반 목록 조회
        List<ClassroomListResponse> teacherClasses = classroomService.findAllByAcademyId(loginUser.getAcademyId(), "ACTIVE");
        model.addAttribute("teacherClasses", teacherClasses);

        return "teacher/student-management";
    }

    /**
     * [AJAX API] 특정 반(classId)에 소속된 학생 목록 JSON 반환
     */
    @GetMapping("/teacher/api/class/{classId}/students")
    @ResponseBody
    public ResponseEntity<List<ClassroomDetailResponse.StudentInfo>> getStudentsByClassId(
            @PathVariable("classId") Long classId) {

        List<ClassroomDetailResponse.StudentInfo> students = classroomService.findStudentsByClassId(classId);
        return ResponseEntity.ok(students);
    }

    /**
     * [선생님용] 특정 학생의 퀘스트(시험) 응시 결과 목록 조회 페이지 렌더링
     */
    @GetMapping("/teacher/student/results")
    public String teacherStudentResultList(
            @RequestParam("studentId") Long studentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            HttpSession session,
            Model model
    ) {
        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);
        if (loginUser == null || loginUser.getRole() != UserRole.TEACHER) {
            return "redirect:/LocalLogin";
        }

        // StudentExamService를 주입받아 사용하거나 필요한 서비스 호출
        // (만약 StudentExamService가 AnalyticsController에 없다면 주입받아 주세요)
        Paging paging = studentExamService.getStudentResultPaging(studentId, date, page);
        List<StudentExamResult> results = studentExamService.getStudentExamResults(studentId, date, paging);
        StudentGrowthSummary growthSummary = studentExamService.getStudentGrowthSummary(studentId);

        model.addAttribute("results", results);
        model.addAttribute("growthSummary", growthSummary);
        model.addAttribute("paging", paging);
        model.addAttribute("selectedDate", date);
        model.addAttribute("studentId", studentId);

        return "layout/exam/result-list";
    }

}