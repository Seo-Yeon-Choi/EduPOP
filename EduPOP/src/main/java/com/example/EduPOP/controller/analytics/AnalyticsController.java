package com.example.EduPOP.controller.analytics;

import com.example.EduPOP.controller.analytics.dto.StudentTrendResponse.SubCategoryStatDto;
import com.example.EduPOP.controller.analytics.dto.ClassWarningResponse;
import com.example.EduPOP.controller.analytics.dto.StudentTrendResponse;
import com.example.EduPOP.controller.classroom.dto.ClassroomListResponse;
import com.example.EduPOP.repository.analytics.AnalyticsMapper;
import com.example.EduPOP.service.analytics.AnalyticsService;
import com.example.EduPOP.service.analytics.ClassWarningService;
import com.example.EduPOP.service.classroom.ClassroomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final ClassWarningService classWarningService;
    private final AnalyticsMapper analyticsMapper;
    private final ClassroomService classroomService; // 💡 기존에 있던 클래스룸 서비스 재활용 (중복 제거)

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

    // 반 전체 성적 분석 및 수업 전 3분 보완 신호 화면 띄우기
    @GetMapping("/class-trend")
    public String viewClassTrend(@RequestParam(name = "examId", required = false) Long examId,
                                 @RequestParam(name = "classId", required = false) Long classId,
                                 @RequestParam(name = "academyId", required = false) Long academyId, // 💡 학원 ID를 파라미터로 받도록 열어둡니다.
                                 Model model) {

        // academyId가 넘어오지 않았다면 기본값으로 1L을 쓰되, 추후 세션이나 DB 조회 결과에 맞게 바꿀 수 있습니다.
        Long targetAcademyId = (academyId != null) ? academyId : 1L;

        // DB에서 해당 학원의 반 목록을 조회합니다.
        List<ClassroomListResponse> teacherClasses = classroomService.findAllByAcademyId(targetAcademyId, "ACTIVE");

        // 만약 ACTIVE 상태로 조회가 안 될 경우를 대비한 방어 코드 (선택 사항)
        if (teacherClasses == null || teacherClasses.isEmpty()) {
            log.warn("학원 ID [{}] 에 해당하는 ACTIVE 반이 없습니다. 전체 조회로 우회합니다.", targetAcademyId);
            // 필요시 상태값 없이 전체 조회하는 서비스 메서드로 변경 가능
        }

        model.addAttribute("teacherClasses", teacherClasses);
        model.addAttribute("examId", examId != null ? examId : 1L);
        model.addAttribute("classId", classId != null ? classId : 1L);

        return "analytics/class-trend";
    }

    /**
     * [AJAX API] 특정 반의 수업 전 3분 보완 신호 및 상세 분석 데이터 JSON 반환
     */
    @GetMapping("/api/class/{classId}/warning-signal")
    @ResponseBody
    public ResponseEntity<ClassWarningResponse> getClassWarningSignal(@PathVariable("classId") Long classId) {

        String className = "담당 반";

        // 1. 해당 반이 응시한 시험별 평균 vs 전체 평균 비교 (null점 방어 포함)
        List<ClassWarningResponse.ExamComparisonDto> examComparisons = analyticsMapper.findExamComparisonsByClassId(classId);

        // 2. 반 전체 취약 유형 TOP 조회 (worst)
        List<SubCategoryStatDto> classWorst = analyticsMapper.findClassCategoryStats(classId, true);
        List<SubCategoryStatDto> classTop = analyticsMapper.findClassCategoryStats(classId, false);

        // 3. 취약 학생 리포트 및 학생별 취약 단어/유형 조회
        List<ClassWarningResponse.StudentVulnerableDto> students = analyticsMapper.findVulnerableStudentsByClassId(classId);
        for (ClassWarningResponse.StudentVulnerableDto student : students) {
            student.setTopWeakWords(analyticsMapper.findTopWeakWordsByStudentId(student.getStudentId()));
            student.setTopWeakTypes(analyticsMapper.findTopWeakTypesByStudentId(student.getStudentId()));
        }

        // 4. 서비스에서 구체적인 위험 사유 문장 생성
        ClassWarningResponse response = classWarningService.evaluateClassWarning(
                classId, className, students, examComparisons, classWorst, classTop
        );

        return ResponseEntity.ok(response);
    }
}