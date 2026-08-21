package com.example.EduPOP.controller.auth;

import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.domain.user.UserRole;
import com.example.EduPOP.service.auth.InvitationService;
import com.example.EduPOP.service.auth.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


// 초대링크를 눌렀을 경우 처리

@Controller
@RequiredArgsConstructor

public class InvitationController {
    private final InvitationService invitationService;

    @GetMapping("/invite")
    public String handleInvitation(@RequestParam String token, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");

        //로그인이 X 경우
        if (loginUser == null) {
            //로그인 페이지로보냄
            return "redirect:/kakao/login";
        }
        //로그인 O인 경우 - 서비스로 확인 후 회원 상태 업데이트
        User updatedUser = invitationService.processInvitation(token, loginUser.getUser_id());
        session.setAttribute("loginUser", updatedUser);

        return switch (updatedUser.getRole()) {
            case ADMIN -> "redirect:/adminMain";
            case TEACHER -> "redirect:/teacherMain";
            case STUDENT -> "redirect:/studentMain";
            default -> "redirect:/";
        };

    }
}
