package com.example.EduPOP.controller.exam;

import com.example.EduPOP.config.SessionConst;
import com.example.EduPOP.controller.exam.dto.ExamListResponse;
import com.example.EduPOP.domain.exam.Exam;
import com.example.EduPOP.domain.exam.ExamQuestion;
import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.domain.user.UserRole;
import com.example.EduPOP.service.classroom.ClassService;
import com.example.EduPOP.service.exam.ExamQuestionParseService;
import com.example.EduPOP.service.exam.ExamService;
import com.example.EduPOP.service.exam.PdfTextExtractService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/teacher/exams")
public class ExamController {

    private final ExamService examService;
    private final ClassService classService;
    private final PdfTextExtractService pdfTextExtractService;
    private final ExamQuestionParseService examQuestionParseService;

    /**
     * 시험 리스트 및 OMR 관리 메인 화면 (좌측 사이드바 반별 필터링 지원)
     */
    @GetMapping
    public String OMRList(
            @RequestParam(name = "classId", required = false) Long classId,
            HttpSession session,
            Model model
    ) {
        Long teacherId = getLoginTeacherId(session);

        if (teacherId == null) {
            return "redirect:/LocalLogin";
        }

        // 1. 좌측 사이드바에 띄울 "내 담당 반 목록" 조회
        var teacherClasses = classService.getClassesByTeacher(teacherId);
        model.addAttribute("classList", teacherClasses);
        model.addAttribute("currentClassId", classId);

        // 해당 반(또는 전체 반)의 시험지
        List<ExamListResponse> exams = examService.getExamListResponseByTeacher(teacherId, classId);

        model.addAttribute("exams", exams);
        return "exam/exam-list";
    }

    @GetMapping("/lists")
    public String examList(HttpSession session, Model model) {
        Long teacherId = getLoginTeacherId(session);

        if (teacherId == null) {
            return "redirect:/LocalLogin";
        }

        model.addAttribute("exams", examService.getExamListByTeacher(teacherId));
        return "layout/exam/list";
    }

    @GetMapping("/create")
    public String createPage(HttpSession session, Model model) {
        Long teacherId = getLoginTeacherId(session);

        if (teacherId == null) {
            return "redirect:/LocalLogin";
        }

        model.addAttribute("classes", classService.getClassesByTeacher(teacherId));
        return "layout/exam/create";
    }

    @GetMapping("/{examId}")
    public String detailPage(
            @PathVariable Long examId,
            HttpSession session,
            Model model
    ) {
        Long teacherId = getLoginTeacherId(session);

        if (teacherId == null) {
            return "redirect:/LocalLogin";
        }

        model.addAttribute("exam", examService.getExamDetailForTeacher(examId, teacherId));
        return "layout/exam/detail";
    }

    @GetMapping("/{examId}/edit")
    public String editPage(
            @PathVariable Long examId,
            HttpSession session,
            Model model
    ) {
        Long teacherId = getLoginTeacherId(session);

        if (teacherId == null) {
            return "redirect:/LocalLogin";
        }

        model.addAttribute("exam", examService.getExamDetailForTeacher(examId, teacherId));
        model.addAttribute("classes", classService.getClassesByTeacher(teacherId));
        return "layout/exam/edit";
    }

    @PostMapping
    @ResponseBody
    public Long createExam(@RequestBody Exam exam, HttpSession session) {
        Long teacherId = getLoginTeacherId(session);

        if (teacherId == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        exam.setTeacherId(teacherId);
        return examService.createExam(exam);
    }

    @PutMapping("/{examId}")
    @ResponseBody
    public Long updateExam(
            @PathVariable Long examId,
            @RequestBody Exam exam,
            HttpSession session
    ) {
        Long teacherId = getLoginTeacherId(session);

        if (teacherId == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        validateAssignedClass(teacherId, exam.getClassId());
        examService.updateExam(examId, teacherId, exam);
        return examId;
    }

    @PostMapping("/parse-pdf")
    @ResponseBody
    public List<ExamQuestion> parsePdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("examType") String examType
    ) {
        String text = pdfTextExtractService.extractText(file);

        if ("WORD".equalsIgnoreCase(examType)) {
            return examQuestionParseService.parseWordExam(text);
        }

        return examQuestionParseService.parseNormalExam(text);
    }

    private Long getLoginTeacherId(HttpSession session) {
        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);

        if (loginUser == null || loginUser.getRole() != UserRole.TEACHER) {
            return null;
        }

        return loginUser.getUserId();
    }

    private void validateAssignedClass(Long teacherId, Long classId) {
        if (classId == null || classService.getClassesByTeacher(teacherId).stream()
                .noneMatch(classItem -> classItem.getClassId().equals(classId))) {
            throw new IllegalArgumentException("담당 반에만 시험을 등록할 수 있습니다.");
        }
    }
}