package com.example.EduPOP.controller.classroom;

import com.example.EduPOP.config.SessionConst;
import com.example.EduPOP.controller.classroom.dto.ClassroomCreateRequest;
import com.example.EduPOP.controller.classroom.dto.ClassroomDetailResponse;
import com.example.EduPOP.controller.classroom.dto.ClassroomListResponse;
import com.example.EduPOP.controller.classroom.dto.ClassroomUpdateRequest;
import com.example.EduPOP.domain.classroom.Classroom;
import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.service.classroom.ClassroomService;
import jakarta.servlet.http.HttpSession;
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

@Slf4j
@Controller
@RequestMapping("/classroom")
@RequiredArgsConstructor
public class ClassroomController {

    private final ClassroomService classroomService;

    // =========================================================
    // 로그인 사용자 조회
    // =========================================================

    private User getLoginUser(HttpSession session) {

        return (User) session.getAttribute(SessionConst.LOGIN_USER);
    }

    // =========================================================
    // 로그인 사용자의 학원 ID 조회
    // =========================================================

    private Long getLoginAcademyId(HttpSession session) {

        User loginUser = getLoginUser(session);

        if (loginUser == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        if (loginUser.getAcademyId() == null) {
            throw new IllegalStateException("소속 학원 정보가 없습니다.");
        }

        return loginUser.getAcademyId();
    }

    // =========================================================
    // 반 개설 화면
    // =========================================================

    @GetMapping("/create")
    public String createForm(
            HttpSession session,
            Model model
    ) {

        User loginUser = getLoginUser(session);

        if (loginUser == null) {
            return "redirect:/LocalLogin";
        }

        Long academyId = loginUser.getAcademyId();

        if (academyId == null) {
            return "redirect:/";
        }

        ClassroomCreateRequest request =
                new ClassroomCreateRequest();

        // 로그인한 관리자의 학원으로 자동 설정
        request.setAcademyId(academyId);

        model.addAttribute("request", request);

        return "classroom/create";
    }

    // =========================================================
    // 신규 반 개설
    // =========================================================

    @PostMapping("/create")
    public String create(
            @Valid @ModelAttribute("request")
            ClassroomCreateRequest request,

            BindingResult bindingResult,

            HttpSession session,
            Model model
    ) {

        User loginUser = getLoginUser(session);

        if (loginUser == null) {
            return "redirect:/LocalLogin";
        }

        Long academyId = loginUser.getAcademyId();

        if (academyId == null) {
            return "redirect:/";
        }

        // -----------------------------------------------------
        // 중요
        //
        // HTML에서 넘어온 academyId를 신뢰하지 않고
        // 현재 로그인 사용자의 academyId로 강제 설정
        // -----------------------------------------------------

        request.setAcademyId(academyId);

        if (bindingResult.hasErrors()) {
            return "classroom/create";
        }

        try {

            classroomService.createClass(request);

        } catch (IllegalArgumentException e) {

            bindingResult.rejectValue(
                    "name",
                    "duplicate",
                    e.getMessage()
            );

            return "classroom/create";
        }

        return "redirect:/classroom/list";
    }

    // =========================================================
    // 반 목록
    // =========================================================

    @GetMapping("/list")
    public String list(
            @RequestParam(
                    name = "status",
                    defaultValue = "ALL"
            )
            String status,

            HttpSession session,
            Model model
    ) {

        User loginUser = getLoginUser(session);

        if (loginUser == null) {
            return "redirect:/LocalLogin";
        }

        Long academyId = loginUser.getAcademyId();

        if (academyId == null) {
            return "redirect:/";
        }

        // 현재 로그인 관리자의 학원 반만 조회
        List<ClassroomListResponse> classList =
                classroomService.findAllByAcademyId(
                        academyId,
                        status
                );

        model.addAttribute(
                "classList",
                classList
        );

        model.addAttribute(
                "currentStatus",
                status
        );

        return "classroom/list";
    }

    // =========================================================
    // 단일 반 상태 변경
    // =========================================================

    @PatchMapping("/{classId}/status")
    @ResponseBody
    public ResponseEntity<String> updateStatus(
            @PathVariable("classId")
            Long classId,

            @RequestBody
            Map<String, String> payload
    ) {

        try {

            String statusStr =
                    payload.get("status");

            Classroom.ClassStatus status =
                    Classroom.ClassStatus.valueOf(statusStr);

            classroomService.updateStatus(
                    classId,
                    status
            );

            return ResponseEntity.ok(
                    "상태가 성공적으로 변경되었습니다."
            );

        } catch (Exception e) {

            log.error(
                    "단일 반 상태 변경 실패: ",
                    e
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    // =========================================================
    // 여러 반 상태 일괄 변경
    // =========================================================

    @PatchMapping("/status/bulk")
    @ResponseBody
    public ResponseEntity<String> updateStatusesBulk(
            @RequestBody
            Map<String, Object> payload
    ) {

        try {

            List<?> rawIds =
                    (List<?>) payload.get("classIds");

            List<Long> classIds =
                    rawIds.stream()
                            .map(id ->
                                    Long.valueOf(
                                            id.toString()
                                    )
                            )
                            .toList();

            String statusStr =
                    (String) payload.get("status");

            Classroom.ClassStatus status =
                    Classroom.ClassStatus.valueOf(
                            statusStr
                    );

            classroomService.updateStatusesBulk(
                    classIds,
                    status
            );

            return ResponseEntity.ok(
                    "일괄 상태 변경이 완료되었습니다."
            );

        } catch (Exception e) {

            log.error(
                    "일괄 상태 변경 실패: ",
                    e
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    // =========================================================
    // 반 상세
    // =========================================================

    @GetMapping("/{classId}")
    public String detail(
            @PathVariable("classId")
            Long classId,

            HttpSession session,
            Model model
    ) {

        User loginUser = getLoginUser(session);

        if (loginUser == null) {
            return "redirect:/LocalLogin";
        }

        Long academyId = loginUser.getAcademyId();

        if (academyId == null) {
            return "redirect:/";
        }

        // 반 상세 정보
        ClassroomDetailResponse classroom =
                classroomService.findById(classId);

        // -----------------------------------------------------
        // 다른 학원의 반을 URL 직접 입력해서 조회하는 것 방지
        // -----------------------------------------------------

        if (!academyId.equals(
                classroom.getAcademyId()
        )) {

            log.warn(
                    "다른 학원 반 접근 시도 - userId={}, academyId={}, classId={}",
                    loginUser.getUserId(),
                    academyId,
                    classId
            );

            return "redirect:/classroom/list";
        }

        // 현재 학원의 강사 목록
        List<ClassroomDetailResponse.TeacherInfo>
                teacherList =
                classroomService
                        .findTeachersByAcademyId(
                                academyId
                        );

        // 현재 학원의 학생 목록
        List<ClassroomDetailResponse.StudentInfo>
                studentPool =
                classroomService
                        .findStudentPool(
                                academyId,
                                classId
                        );

        model.addAttribute(
                "teacherList",
                teacherList
        );

        model.addAttribute(
                "classroom",
                classroom
        );

        model.addAttribute(
                "studentPool",
                studentPool
        );

        return "classroom/detail";
    }

    // =========================================================
    // 반 기본 정보 수정
    // =========================================================

    @PutMapping("/{classId}")
    @ResponseBody
    public ResponseEntity<?> updateClass(
            @PathVariable("classId")
            Long classId,

            @Valid
            @RequestBody
            ClassroomUpdateRequest request,

            BindingResult bindingResult
    ) {

        if (bindingResult.hasErrors()) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            bindingResult
                                    .getFieldError()
                                    .getDefaultMessage()
                    );
        }

        classroomService.updateClass(
                classId,
                request
        );

        return ResponseEntity.ok("OK");
    }

    // =========================================================
    // 강사 추가 배정
    // =========================================================

    @PostMapping("/{classId}/teachers")
    @ResponseBody
    public ResponseEntity<?> addTeacher(
            @PathVariable("classId")
            Long classId,

            @RequestBody
            ClassroomCreateRequest.TeacherRequest request
    ) {

        classroomService.addTeacher(
                classId,
                request.getTeacherId(),
                request.getRoleType()
        );

        return ResponseEntity.ok("OK");
    }

    // =========================================================
    // 강사 배정 해제
    // =========================================================

    @DeleteMapping("/{classId}/teachers/{teacherId}")
    @ResponseBody
    public ResponseEntity<?> removeTeacher(
            @PathVariable("classId")
            Long classId,

            @PathVariable("teacherId")
            Long teacherId
    ) {

        classroomService.removeTeacher(
                classId,
                teacherId
        );

        return ResponseEntity.ok("OK");
    }

    // =========================================================
    // 수강생 추가 배정
    // =========================================================

    @PostMapping("/{classId}/students")
    @ResponseBody
    public ResponseEntity<?> addStudent(
            @PathVariable("classId")
            Long classId,

            @RequestBody
            Map<String, Long> payload
    ) {

        Long studentId =
                payload.get("studentId");

        if (studentId == null) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            "학생 식별자가 누락되었습니다."
                    );
        }

        try {

            classroomService.addStudent(
                    classId,
                    studentId
            );

            return ResponseEntity.ok(
                    "STUDENT_ADDED"
            );

        } catch (
                IllegalStateException |
                IllegalArgumentException e
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // =========================================================
    // 수강생 제외
    // =========================================================

    @DeleteMapping("/{classId}/students/{studentId}")
    @ResponseBody
    public ResponseEntity<?> removeStudent(
            @PathVariable("classId")
            Long classId,

            @PathVariable("studentId")
            Long studentId
    ) {

        classroomService.removeStudent(
                classId,
                studentId
        );

        return ResponseEntity.ok(
                "STUDENT_REMOVED"
        );
    }

    // =========================================================
    // 수강생 일괄 동기화
    // =========================================================

    @PostMapping("/{classId}/students/sync")
    @ResponseBody
    public ResponseEntity<String> syncStudents(
            @PathVariable("classId")
            Long classId,

            @RequestBody
            Map<String, Object> payload
    ) {

        try {

            List<?> rawIds =
                    (List<?>) payload.get(
                            "studentIds"
                    );

            List<Long> studentIds =
                    new java.util.ArrayList<>();

            if (rawIds != null) {

                for (Object id : rawIds) {

                    studentIds.add(
                            Long.valueOf(
                                    id.toString()
                            )
                    );
                }
            }

            classroomService.syncStudents(
                    classId,
                    studentIds
            );

            return ResponseEntity.ok(
                    "수강생 동기화 완료"
            );

        } catch (Exception e) {

            log.error(
                    "수강생 동기화 처리 에러: ",
                    e
            );

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(e.getMessage());
        }
    }
}