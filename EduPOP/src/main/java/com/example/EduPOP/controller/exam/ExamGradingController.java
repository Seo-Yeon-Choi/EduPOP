package com.example.EduPOP.controller.exam;

import com.example.EduPOP.config.SessionConst;
import com.example.EduPOP.controller.classroom.dto.ClassroomListResponse;
import com.example.EduPOP.controller.exam.dto.*;
import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.service.classroom.ClassroomService;
import com.example.EduPOP.service.exam.ExamService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/exam")
@RequiredArgsConstructor
public class ExamGradingController {

    private final ExamService examService;
    private final ClassroomService classroomService;

    @GetMapping("/list")
    public String viewExamList(
            @RequestParam(name = "classId", required = false) Long classId,
            HttpSession session,
            Model model
    ) {

        User loginUser =
                (User) session.getAttribute(SessionConst.LOGIN_USER);

        if (loginUser == null) {
            return "redirect:/login";
        }

        Long academyId = loginUser.getAcademyId();
        Long teacherId = loginUser.getUserId();

        // 로그인한 사용자의 학원에 속한 반 목록
        List<ClassroomListResponse> classList =
                classroomService.findAllByAcademyId(
                        academyId,
                        null
                );

        model.addAttribute("classList", classList);

        if (classId == null && classList != null && !classList.isEmpty()) {
        }

        // 시험 목록
        List<ExamListResponse> examList =
                examService.getAllExamList();

        // 특정 반이 선택된 경우 해당 반 시험만 필터링
        if (classId != null) {
            final Long targetClassId = classId;
            examList = examList.stream()
                    .filter(exam ->
                            targetClassId.equals(exam.getClassId()))
                    .toList();
        }

        model.addAttribute("exams", examList);
        model.addAttribute("currentClassId", classId);

        // 로그인한 교사가 만든 시험 목록
        List<ExamTemplateResponse> templates =
                examService.getExamTemplatesByTeacher(teacherId);

        model.addAttribute("templates", templates);

        return "exam/exam-list";
    }

    @GetMapping({"/omr-matrix", "/{examId}/omr"})
    public String viewOmrMatrix(
            @RequestParam(
                    name = "examId",
                    required = false
            )
            Long examIdParam,

            @PathVariable(
                    name = "examId",
                    required = false
            )
            Long examIdPath,

            HttpSession session,
            Model model
    ) {

        User loginUser =
                (User) session.getAttribute(SessionConst.LOGIN_USER);

        if (loginUser == null) {
            return "redirect:/login";
        }

        Long examId =
                examIdPath != null
                        ? examIdPath
                        : examIdParam;

        if (examId == null) {
            return "redirect:/exam/list";
        }

        log.info(
                "OMR 채점 화면 진입 - teacherId: {}, examId: {}",
                loginUser.getUserId(),
                examId
        );

        ExamDetailResponse examDetail =
                examService.getExamDetailForOmr(examId);

        model.addAttribute("exam", examDetail);

        return "exam/omr-matrix";
    }

    @PostMapping("/api/create-sheet")
    @ResponseBody
    public ResponseEntity<Long> createExamSheet(
            @RequestBody ExamCreateOrCopyRequest request,
            HttpSession session
    ) {

        User loginUser =
                (User) session.getAttribute(SessionConst.LOGIN_USER);

        if (loginUser == null) {
            return ResponseEntity
                    .status(401)
                    .build();
        }

        /*
         * academyId는 프론트에서 전달받은 값을 믿지 않고
         * 로그인한 사용자의 세션 값을 사용
         */
        request.setAcademyId(
                loginUser.getAcademyId()
        );

        // 프론트에서 받은 값 대신 로그인 세션의 교사 번호를 사용
        request.setTeacherId(
                loginUser.getUserId()
        );

        log.info(
                "새 시험 배정 요청 - teacherId: {}, academyId: {}, classId: {}",
                loginUser.getUserId(),
                loginUser.getAcademyId(),
                request.getClassId()
        );

        Long examId =
                examService.createExamSheet(request);

        return ResponseEntity.ok(examId);
    }

    @PostMapping("/api/save-grades")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveBulkGrades(
            @RequestBody ExamBulkGradeRequest request,
            HttpSession session
    ) {

        User loginUser =
                (User) session.getAttribute(SessionConst.LOGIN_USER);

        if (loginUser == null) {
            return ResponseEntity
                    .status(401)
                    .build();
        }

        log.info(
                "시험 ID [{}] 일괄 채점 저장 요청 - teacherId: {}",
                request.getExamId(),
                loginUser.getUserId()
        );

        examService.saveBulkGrades(
                request,
                loginUser.getUserId()
        );

        Map<String, Object> response =
                new HashMap<>();

        response.put("success", true);
        response.put(
                "message",
                "채점 결과가 성공적으로 저장되었습니다."
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getExamStats(
            @RequestParam("examId") Long examId,

            @RequestParam(
                    value = "classId",
                    required = false
            )
            Long classId,

            HttpSession session
    ) {

        User loginUser =
                (User) session.getAttribute(SessionConst.LOGIN_USER);

        if (loginUser == null) {
            return ResponseEntity
                    .status(401)
                    .build();
        }

        Map<String, Object> stats =
                examService.getExamStats(
                        examId,
                        classId
                );

        return ResponseEntity.ok(stats);
    }

    @PostMapping("/api/save-comments")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveTeacherComments(
            @RequestBody ExamCommentSaveRequest request,

            HttpSession session
    ) {

        User loginUser =
                (User) session.getAttribute(SessionConst.LOGIN_USER);

        if (loginUser == null) {
            return ResponseEntity
                    .status(401)
                    .build();
        }

        List<ExamCommentSaveRequest.StudentCommentPayload> comments =
                request.getComments();

        if (comments != null &&
                !comments.isEmpty()) {

            examService.saveTeacherComments(
                    request.getExamId(),
                    loginUser.getUserId(),
                    comments
            );
        }

        Map<String, Object> response =
                new HashMap<>();

        response.put("success", true);

        return ResponseEntity.ok(response);
    }
}

