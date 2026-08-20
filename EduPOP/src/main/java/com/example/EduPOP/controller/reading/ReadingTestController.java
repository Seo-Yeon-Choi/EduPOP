package com.example.EduPOP.controller.reading;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller // 독서 기능을 확인할 임시 테스트 요청 처리
public class ReadingTestController {

    @GetMapping("/test/reading/student") // 테스트 학생 세션 생성
    public String testStudentLogin(
            @RequestParam("userId") Long studentId,
            HttpSession session
    ) {
        session.setAttribute(
                "loginUserId",
                studentId
        ); // 테스트 학생 번호를 로그인 세션에 저장

        return "redirect:/student/reading"; // 학생 독서 목록 화면으로 이동
    }


    @GetMapping("/test/reading/teacher") // 테스트 교사 세션 생성
    public String testTeacherLogin(
            @RequestParam("userId") Long teacherId,
            HttpSession session
    ) {
        session.setAttribute(
                "loginUserId",
                teacherId
        ); // 테스트 교사 번호를 로그인 세션에 저장

        return "redirect:/teacher/reading"; // 교사 독서 목록 화면으로 이동
    }


    @GetMapping("/test/reading/logout") // 테스트 로그인 세션 삭제
    public String testLogout(
            HttpSession session
    ) {
        session.invalidate(); // 현재 테스트 세션의 모든 로그인 정보 삭제

        return "redirect:/login"; // 로그인 주소로 이동
    }
}