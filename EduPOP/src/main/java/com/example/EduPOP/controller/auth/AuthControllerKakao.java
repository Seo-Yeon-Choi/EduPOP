package com.example.EduPOP.controller.auth;

import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.domain.user.UserRole;
import com.example.EduPOP.domain.user.UserStatus;
import com.example.EduPOP.service.auth.KakaoService;
import com.example.EduPOP.service.auth.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthControllerKakao {

    private final KakaoService kakaoService;
    private final UserService userService;

    @GetMapping("/kakao/login/student")
    public String kakaoStudentLogin(HttpSession session) {
        session.setAttribute("requestedRole", "STUDENT");
        return "redirect:/kakao/login";
    }

    // 교사가 누르는 카카오 로그인 버튼
    @GetMapping("/kakao/login/teacher")
    public String kakaoTeacherLogin(HttpSession session) {
        session.setAttribute("requestedRole", "TEACHER");
        return "redirect:/kakao/login";
    }

    // 관리자가 누르는 카카오 로그인 버튼
    @GetMapping("/kakao/login/admin")
    public String kakaoAdminLogin(HttpSession session) {
        session.setAttribute("requestedRole", "ADMIN");
        return "redirect:/kakao/login";
    }

    // 카카오 로그인 버튼 누르면 카카오로 안내
    @GetMapping("/kakao/login")
    public String kakaoLogin() {
        String kakaoAddress = "https://kauth.kakao.com/oauth/" +
                "authorize?client_id=f0d17d7cf78033e1ed7f979b9b09591b" +
                "&redirect_uri=http://localhost:8080/kakao/callback&response_type=code" +
                "&prompt=login";

        return "redirect:" + kakaoAddress;
    }

    // user가 인증코드 들고 옴
    @GetMapping("/kakao/callback")
    public String kakaoCallback(
            @RequestParam String code,
            HttpSession session
    ) {
        String requestedRole = (String) session.getAttribute("requestedRole");

        if (requestedRole == null) {
            requestedRole = "NONE";
        }

        User kakaoUser = kakaoService.loginWithKakao(
                code,
                UserRole.valueOf(requestedRole)
        );

        User latestUser = userService.findByUserId(kakaoUser.getUserId());

        if (latestUser != null) {
            kakaoUser = latestUser;
        }

        session.setAttribute("loginUser", kakaoUser);

        if (kakaoUser.getStatus() == UserStatus.PENDING) {
            if (kakaoUser.getRole() == UserRole.ADMIN) {
                return "redirect:/adminWaiting";
            } else {
                return "redirect:/blankPage";
            }
        } else if (kakaoUser.getStatus() == UserStatus.ACTIVE) {
            if (kakaoUser.getRole() == UserRole.STUDENT) {
                return "/main/studentMain";
            } else if (kakaoUser.getRole() == UserRole.TEACHER) {
                return "/main/teacherMain";
            } else if (kakaoUser.getRole() == UserRole.ADMIN) {
                return "main/adminMain";
            }
        }

        return "redirect:/";
    }
}

