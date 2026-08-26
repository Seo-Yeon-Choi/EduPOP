package com.example.EduPOP.controller.exam;

import com.example.EduPOP.controller.classroom.dto.ClassroomListResponse;
import com.example.EduPOP.controller.exam.dto.*;
import com.example.EduPOP.service.classroom.ClassroomService;
import com.example.EduPOP.service.exam.ExamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * [역할: 반별 시험지 배정 및 OMR 고속 채점 웹 컨트롤러]
 */
@Slf4j
@Controller
@RequestMapping("/exam")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;
    private final ClassroomService classroomService; // 💡 기존 반 관리 서비스 재활용

    /**
     * [화면 1: 반 중심 시험 및 채점 관리 메인 워크스페이스]
     * URL: http://localhost:8080/exam/list
     */
    @GetMapping("/list")
    public String viewExamList(@RequestParam(name = "classId", required = false) Long classId, Model model) {
        log.info("반 중심 시험 관리 대시보드 화면 요청 (조회 반 ID: {})", classId);

        // 1. 좌측 전체 반 목록 렌더링
        List<ClassroomListResponse> classList = classroomService.findAllByAcademyId(1L, null);
        model.addAttribute("classList", classList);

        // 2. 전체 시험 목록 조회
        List<ExamListResponse> examList = examService.getAllExamList();

        // 💡 [핵심] classId가 넘어왔다면(특정 반을 클릭했다면) 해당 반의 시험만 필터링합니다.
        if (classId != null) {
            examList = examList.stream()
                    .filter(exam -> classId.equals(exam.getClassId()))
                    .toList();
        }
        model.addAttribute("exams", examList);

        // 💡 [추가] 프론트엔드에서 어떤 반이 클릭되었는지 알고 파란색 표시를 할 수 있게 값을 넘겨줍니다.
        model.addAttribute("currentClassId", classId);

        // 3. 모달창 드롭다운용 템플릿 목록
        List<ExamTemplateResponse> templates = examService.getExamTemplates(1L);
        model.addAttribute("templates", templates);

        return "exam/exam-list";
    }

    /**
     * [화면 2: 스마트 OMR 고속 채점판 화면]
     * URL: http://localhost:8080/exam/omr-matrix?examId=1 또는 /exam/1/omr
     */
    @GetMapping({"/omr-matrix", "/{examId}/omr"})
    public String viewOmrMatrix(@RequestParam(name = "examId", required = false) Long examIdParam,
                                @PathVariable(name = "examId", required = false) Long examIdPath,
                                Model model) {
        Long examId = (examIdPath != null) ? examIdPath : examIdParam;
        if (examId == null) examId = 1L;

        log.info("OMR 채점 화면 진입: examId = [{}]", examId);
        ExamDetailResponse examDetail = examService.getExamDetailForOmr(examId);
        model.addAttribute("exam", examDetail);

        return "exam/omr-matrix";
    }

    /**
     * [API 1: 반에 새 시험 배정 생성]
     */
    @PostMapping("/api/create-sheet")
    @ResponseBody
    public ResponseEntity<Long> createExamSheet(@RequestBody ExamCreateOrCopyRequest request) {
        Long examId = examService.createExamSheet(request);
        return ResponseEntity.ok(examId);
    }

    /**
     * [API 2: OMR 일괄 채점 저장]
     */
    @PostMapping("/api/save-grades")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveBulkGrades(@RequestBody ExamBulkGradeRequest request) {
        log.info("시험 ID [{}] 일괄 채점 저장 요청 접수", request.getExamId());
        examService.saveBulkGrades(request);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "채점 결과가 성공적으로 저장되었습니다.");
        return ResponseEntity.ok(response);
    }

    /**
     * [API 3: 성적 분석(반 평균, 전체 평균) 데이터 반환]
     */
    @GetMapping("/api/stats")
    @ResponseBody
    public Map<String, Object> getExamStats(@RequestParam("examId") Long examId,
                                            @RequestParam(value = "classId", required = false) Long classId) {
        return examService.getExamStats(examId, classId);
    }

    /**
     * [API 4: 학부모 리포트 코멘트만 즉시 저장]
     */
    @PostMapping("/api/save-comments")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveTeacherComments(@RequestBody Map<String, List<Map<String, Object>>> payload) {
        List<Map<String, Object>> comments = payload.get("comments");
        if (comments != null && !comments.isEmpty()) {
            examService.saveTeacherComments(comments);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
}