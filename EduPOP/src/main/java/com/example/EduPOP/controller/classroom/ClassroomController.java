package com.example.EduPOP.controller.classroom;

import com.example.EduPOP.controller.classroom.dto.ClassroomCreateRequest;
import com.example.EduPOP.controller.classroom.dto.ClassroomListResponse;
import com.example.EduPOP.service.classroom.ClassroomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 반 관리 컨트롤러
 * - 반 생성 폼 이동 및 등록 요청 처리
 */
@Controller
@RequestMapping("/classroom")
@RequiredArgsConstructor
public class ClassroomController {
    private final ClassroomService classroomService;

    // 반 개설 화면 이동
    @GetMapping("/create")
    public String createForm(Model model){
        ClassroomCreateRequest request = new ClassroomCreateRequest();
        // 💡 로그인한 운영자의 학원 ID를 자동으로 세팅 (현재는 테스트용 1번)
        request.setAcademyId(1L);
        // 💡 학원 소속 강사 목록 조회 (현재는 더미 또는 서비스/매퍼 조회)
//        List<User> teacherList = userService.findTeachersByAcademyId(1L);
//        model.addAttribute("teacherList", teacherList);

        model.addAttribute("request", request);
        return "classroom/create";
    }

    // 신규 반 개설 및 강사 배정 처리 (DTO 도입 및 @Valid 유효성 검증 적용)
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("request") ClassroomCreateRequest request,
                         BindingResult bindingResult,
                         Model model) {

        // 유효성 검증 실패 시 (빈칸, 글자수 초과, 음수 정원 등)
        if (bindingResult.hasErrors()) {
            // 에러를 품은 채로 다시 작성 화면(create.html)으로 이동
            return "classroom/create";
        }

        // 비즈니스 로직 실행 및 중복 예외 처리
        try {
            // 정상 통과 시 비즈니스 로직 실행
            classroomService.createClass(request);
        } catch (IllegalArgumentException e) {
            // 💡 핵심: 서비스에서 던진 "이미 존재하는 반 이름입니다." 에러를 name 필드의 화면 오류로 바인딩!
            bindingResult.rejectValue("name", "duplicate", e.getMessage());
            return "classroom/create"; // 500 에러 대신 친절하게 폼 화면으로 복귀
        }

        return "redirect:/classroom/list";
    }
        // 반 목록 화면 매핑
        @GetMapping("/list")
        public String list(@RequestParam(name = "status", defaultValue = "ALL") String status, Model model) {
            Long academyId = 1L; // 💡 현재 테스트용 학원 ID (추후 세션/로그인 연동)

            // Service 호출하여 데이터 가져오기
            List<ClassroomListResponse> classList = classroomService.findAllByAcademyId(academyId,status);

            // 화면(Thymeleaf)으로 데이터를 넘겨주기 위해 Model에 담기
            model.addAttribute("classList", classList);
            model.addAttribute("currentStatus", status);


            return "classroom/list"; // templates/classroom/list.html 화면 호출
        }

        // 단건 종강 처리
        @PostMapping("/{classId}/status")
        @ResponseBody
        public ResponseEntity<String> updateStatus(
                @PathVariable("classId") Long classId,
                @RequestParam("status") String status) {

            classroomService.updateStatus(classId, status);
            return ResponseEntity.ok("OK");
        }

        // 다중 일괄 종강 처리
        @PostMapping("/status/bulk")
        @ResponseBody
        public ResponseEntity<String> updateStatusesBulk(@RequestBody Map<String, Object> payload) {
            @SuppressWarnings("unchecked")
            List<Integer> rawIds = (List<Integer>) payload.get("classIds");
            String status = (String) payload.get("status");

            if (rawIds == null || rawIds.isEmpty()) {
                return ResponseEntity.badRequest().body("선택된 반이 없습니다.");
            }

            List<Long> classIds = rawIds.stream().map(Number::longValue).toList();
            classroomService.updateStatusesBulk(classIds, status);

            return ResponseEntity.ok("OK");
        }
}
