package com.example.EduPOP.controller.classroom;

import com.example.EduPOP.controller.classroom.dto.ClassroomCreateRequest;
import com.example.EduPOP.controller.classroom.dto.ClassroomDetailResponse;
import com.example.EduPOP.controller.classroom.dto.ClassroomListResponse;
import com.example.EduPOP.controller.classroom.dto.ClassroomUpdateRequest;
import com.example.EduPOP.domain.classroom.Classroom;
import com.example.EduPOP.service.classroom.ClassroomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
@Slf4j
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

        return "redirect:admin/classroom/list";
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

    /**
     *  단일 반 상태 변경 (종강 / 재개)
     */
    @PatchMapping("/{classId}/status")
    @ResponseBody
    public ResponseEntity<String> updateStatus(
            @PathVariable("classId") Long classId,
            @RequestBody Map<String, String> payload) {
        try {
            String statusStr = payload.get("status");
            Classroom.ClassStatus status = Classroom.ClassStatus.valueOf(statusStr);
            classroomService.updateStatus(classId, status);
            return ResponseEntity.ok("상태가 성공적으로 변경되었습니다.");
        } catch (Exception e) {
            log.error("단일 반 상태 변경 실패: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * 💡 2. 선택된 반 다중 일괄 상태 변경 (일괄 종강 / 재개)
     */
    @PatchMapping("/status/bulk")
    @ResponseBody
    public ResponseEntity<String> updateStatusesBulk(@RequestBody Map<String, Object> payload) {
        try {
            List<?> rawIds = (List<?>) payload.get("classIds");
            List<Long> classIds = rawIds.stream()
                    .map(id -> Long.valueOf(id.toString()))
                    .toList();

            String statusStr = (String) payload.get("status");
            Classroom.ClassStatus status = Classroom.ClassStatus.valueOf(statusStr);

            classroomService.updateStatusesBulk(classIds, status);
            return ResponseEntity.ok("일괄 상태 변경이 완료되었습니다.");
        } catch (Exception e) {
            log.error("일괄 상태 변경 실패: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // 반 상세 정보 화면 이동
    @GetMapping("/{classId}")
    public String detail(@PathVariable("classId") Long classId, Model model) {
        // 반 상세 정보 조회 (기본 정보 + 강사 목록 + 학생 목록)
        ClassroomDetailResponse classroom = classroomService.findById(classId);

        // 학원에 소속된 전체 강사 목록 조회 (드롭다운 토글용)
        // userService 또는 classroomService에서 학원 ID 기준 강사 목록을 가져옴
        List<ClassroomDetailResponse.TeacherInfo> teacherList = classroomService.findTeachersByAcademyId(classroom.getAcademyId());

        // 학원 전체 학생 풀 조회하여 모델에 담기
        List<ClassroomDetailResponse.StudentInfo> studentPool =
                classroomService.findStudentPool(classroom.getAcademyId(), classId);

        model.addAttribute("teacherList", teacherList);
        model.addAttribute("classroom", classroom);
        model.addAttribute("studentPool", studentPool);

        return "classroom/detail";
    }

    // 반 기본 정보 전체 수정 (PUT)
    @PutMapping("/{classId}")
    @ResponseBody
    public ResponseEntity<?> updateClass(
            @PathVariable("classId") Long classId,
            @Valid @RequestBody ClassroomUpdateRequest request,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getFieldError().getDefaultMessage());
        }

        classroomService.updateClass(classId, request);
        return ResponseEntity.ok("OK");
    }
    // 강사 신규 추가 배정 (POST)
    @PostMapping("/{classId}/teachers")
    @ResponseBody
    public ResponseEntity<?> addTeacher(
            @PathVariable("classId") Long classId,
            @RequestBody ClassroomCreateRequest.TeacherRequest request) {

        classroomService.addTeacher(classId, request.getTeacherId(), request.getRoleType());
        return ResponseEntity.ok("OK");
    }

    // 강사 배정 해제 (DELETE)
    @DeleteMapping("/{classId}/teachers/{teacherId}")
    @ResponseBody
    public ResponseEntity<?> removeTeacher(
            @PathVariable("classId") Long classId,
            @PathVariable("teacherId") Long teacherId) {

        classroomService.removeTeacher(classId, teacherId);
        return ResponseEntity.ok("OK");
    }

    // 수강생 추가 배정
    @PostMapping("/{classId}/students")
    @ResponseBody
    public ResponseEntity<?> addStudent(
            @PathVariable("classId") Long classId,
            @RequestBody Map<String, Long> payload) {

        Long studentId = payload.get("studentId");
        if (studentId == null) {
            return ResponseEntity.badRequest().body("학생 식별자가 누락되었습니다.");
        }

        try {
            classroomService.addStudent(classId, studentId);
            return ResponseEntity.ok("STUDENT_ADDED");
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 수강생 퇴원 / 제외
    @DeleteMapping("/{classId}/students/{studentId}")
    @ResponseBody
    public ResponseEntity<?> removeStudent(
            @PathVariable("classId") Long classId,
            @PathVariable("studentId") Long studentId) {

        classroomService.removeStudent(classId, studentId);
        return ResponseEntity.ok("STUDENT_REMOVED");
    }

    @PostMapping("/{classId}/students/sync")
    @ResponseBody
    public ResponseEntity<String> syncStudents(
            @PathVariable("classId") Long classId,
            @RequestBody Map<String, Object> payload) {
        try {
            List<?> rawIds = (List<?>) payload.get("studentIds");
            List<Long> studentIds = new java.util.ArrayList<>();

            if (rawIds != null) {
                for (Object id : rawIds) {
                    studentIds.add(Long.valueOf(id.toString()));
                }
            }

            classroomService.syncStudents(classId, studentIds);
            return ResponseEntity.ok("수강생 동기화 완료");
        } catch (Exception e) {
            log.error("수강생 동기화 처리 에러: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


}