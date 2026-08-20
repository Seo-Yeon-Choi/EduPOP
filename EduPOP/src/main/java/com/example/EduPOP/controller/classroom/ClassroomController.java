package com.example.EduPOP.controller.classroom;

import com.example.EduPOP.domain.classroom.Classroom;
import com.example.EduPOP.service.classroom.ClassroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
    public String createForm(){
        return "classroom/create"; // templates/classroom/create.html 파일 매핑
    }

    // 신규 반 개설 및 강사 배정 처리
    @PostMapping("/create")
    public String create(Classroom classroom){
        classroomService.createClass(classroom);
        return "redirect:/classroom/list";
    }

}
