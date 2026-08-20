package com.example.EduPOP.controller.reading;

import com.example.EduPOP.controller.reading.dto.ReadingReportCreateRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller // 독서록 관련 URL 요청을 받는 Controller로 등록
public class ReadingReportController {

    private static final int MIN_CONTENT_LENGTH = 50; // 제출 가능한 독서록 최소 글자 수
    private static final int MAX_CONTENT_LENGTH = 10_000; // 독서록 최대 글자 수

    @GetMapping("/student/reading") // 기존 독서 메뉴 요청을 독서록 작성 화면으로 이동
    public String moveToCreatePage() {
        return "redirect:/student/reading-reports/create";
    }

    @GetMapping("/student/reading-reports/create") // 독서록 작성 화면 요청 처리
    public String showCreatePage(Model model) {
        model.addAttribute(
                "readingReportCreateRequest",
                new ReadingReportCreateRequest()
        ); // 화면에서 사용할 빈 독서록 입력 객체 전달
        model.addAttribute("activeMenu", "reading-create"); // 독서록 작성 메뉴 활성화
        return "student/reading-report/create"; // 독서록 작성 HTML 화면 반환
    }

    @PostMapping("/student/reading-reports/create") // 작성 화면에서 전송한 독서록 처리
    public String receiveCreateRequest(
            @ModelAttribute("readingReportCreateRequest") ReadingReportCreateRequest request,
            @RequestParam(name = "action", defaultValue = "DRAFT") String action,
            Model model) {

        List<String> errors = validate(request, action); // 서버에서 입력값과 요청 상태 확인

        model.addAttribute("activeMenu", "reading-create"); // 화면을 다시 보여줄 때 메뉴 활성화 유지
        model.addAttribute("requestedAction", action); // 사용자가 누른 버튼의 상태를 화면에 전달

        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors); // 확인된 오류 목록을 화면에 전달
            return "student/reading-report/create";
        }

        if ("SUBMITTED".equals(action)) {
            model.addAttribute(
                    "successMessage",
                    "독서록 제출 요청을 서버가 정상적으로 받았습니다. DB 저장은 다음 단계에서 연결합니다."
            ); // 제출 요청을 정상적으로 받은 결과 전달
        } else {
            model.addAttribute(
                    "successMessage",
                    "독서록 임시 저장 요청을 서버가 정상적으로 받았습니다. DB 저장은 다음 단계에서 연결합니다."
            ); // 임시 저장 요청을 정상적으로 받은 결과 전달
        }

        return "student/reading-report/create";
    }

    private List<String> validate(ReadingReportCreateRequest request, String action) {
        List<String> errors = new ArrayList<>(); // 화면에 보여줄 오류 문구 저장

        if (!"DRAFT".equals(action) && !"SUBMITTED".equals(action)) {
            errors.add("저장 방식을 다시 선택해 주세요.");
            return errors;
        }

        if (request.getBookTitle() == null || request.getBookTitle().isBlank()) {
            errors.add("책 제목을 입력해 주세요.");
        }

        if (request.getReportTitle() == null || request.getReportTitle().isBlank()) {
            errors.add("독서록 제목을 입력해 주세요.");
        }

        String content = request.getContent() == null ? "" : request.getContent().trim();

        if (content.length() > MAX_CONTENT_LENGTH) {
            errors.add("독서록 내용은 10,000자 이하로 작성해 주세요.");
        }

        if ("SUBMITTED".equals(action) && content.length() < MIN_CONTENT_LENGTH) {
            errors.add("독서록을 제출하려면 내용을 50자 이상 작성해 주세요.");
        }

        return errors;
    }
}
