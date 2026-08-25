package com.example.EduPOP.controller.exam;

import com.example.EduPOP.domain.exam.Exam;
import com.example.EduPOP.domain.exam.ExamQuestion;
import com.example.EduPOP.service.classroom.ClassService;
import com.example.EduPOP.service.exam.ExamQuestionParseService;
import com.example.EduPOP.service.exam.ExamService;
import com.example.EduPOP.service.exam.PdfTextExtractService;
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

    // =========================================
    // 시험지 목록
    // =========================================

    @GetMapping
    public String examList(Model model) {

        model.addAttribute("exams", examService.getExamList());

        return "layout/exam/list";
    }

    // =========================================
    // 시험지 생성 화면
    // =========================================

    @GetMapping("/create")
    public String createPage(Model model) {
        /*
         * 현재 테스트용
         * 추후 로그인 세션에서 teacherId 가져오기
         */
        Long teacherId = 1L;

        model.addAttribute("classes", classService.getClassesByTeacher(teacherId));

        return "layout/exam/create";
    }

    // =========================================
    // 시험지 등록
    // =========================================

    @PostMapping
    @ResponseBody
    public Long createExam(@RequestBody Exam exam) {
        /*
         * 현재 테스트용
         * 추후 세션에서 가져오기
         */
        Long teacherId = 1L;

        exam.setTeacherId(teacherId);

        System.out.println("classId = " + exam.getClassId());

        System.out.println("teacherId = " + exam.getTeacherId());

        return examService.createExam(exam);
    }

    // =========================================
    // PDF 문제 추출
    // =========================================

    @PostMapping("/parse-pdf")
    @ResponseBody
    public List<ExamQuestion> parsePdf(@RequestParam("file") MultipartFile file, @RequestParam("examType") String examType) {

        // -------------------------------------
        // PDFBox 텍스트 추출
        // -------------------------------------

        String text = pdfTextExtractService.extractText(file);

        // 개발 중 확인용
        System.out.println("=================================");

        System.out.println("PDF 추출 결과");

        System.out.println("examType = " + examType);

        System.out.println(text);

        System.out.println("=================================");

        // =====================================
        // 단어 시험
        // =====================================

        if ("WORD".equalsIgnoreCase(examType)) {
            return examQuestionParseService.parseWordExam(text);
        }

        // =====================================
        // 일반 시험
        // =====================================

        return examQuestionParseService.parseNormalExam(text);
    }
}