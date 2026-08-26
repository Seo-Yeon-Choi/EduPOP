package com.example.EduPOP.controller.analytics;

import com.example.EduPOP.controller.analytics.dto.StudentTrendResponse;
import com.example.EduPOP.service.analytics.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/student-trend")
    public String viewStudentTrend(@RequestParam(name = "studentId", required = false) Long studentId,
                                   @RequestParam(name = "examId", required = false) Long examId,
                                   Model model) {
        if (studentId == null) studentId = 7L;

        log.info("학생 ID [{}] 성적 분석 리포트 조회 (연결된 시험 ID: [{}])", studentId, examId);
        StudentTrendResponse trendData = analyticsService.getStudentTrendData(studentId);

        model.addAttribute("report", trendData);
        // 500 에러 방어: examId가 null이어도 안전하게 기본값 부여
        model.addAttribute("currentExamId", examId != null ? examId : 10L);

        return "analytics/student-trend";
    }

    // 반 전체 성적 분석 전용 화면 띄우기
    @GetMapping("/class-trend")
    public String viewClassTrend(@RequestParam("examId") Long examId,
                                 @RequestParam(name = "classId", required = false) Long classId,
                                 Model model) {
        // 프론트엔드에서 API를 호출할 수 있도록 아이디만 넘겨줍니다.
        model.addAttribute("examId", examId);
        model.addAttribute("classId", classId != null ? classId : 1L);
        return "analytics/class-trend"; // 새로운 HTML 파일 연결
    }
}