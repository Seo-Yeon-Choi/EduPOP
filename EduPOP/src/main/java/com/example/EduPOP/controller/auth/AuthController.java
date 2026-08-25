package com.example.EduPOP.controller.auth;

import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.domain.user.UserRole;
import com.example.EduPOP.domain.user.UserStatus;
import com.example.EduPOP.service.auth.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    // 기본 도메인 요청 시 로그인 페이지로 이동
    @GetMapping("/")
    public String home() {
        return "/mainHomePage";
    }

    // 회원가입 화면
    @GetMapping("/signUp")
    public String signUpPage() {
        return "signUp";
    }

    // 회원가입 처리
    @PostMapping("/signUp")
    public String signUpProcess(
            @ModelAttribute User user,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        String requestedRole = (String) session.getAttribute("requestedRole");

        if (requestedRole == null) {
            requestedRole = "NONE";
        }

        user.setRole(UserRole.valueOf(requestedRole));

        boolean success = userService.registerLocalUser(user);

        if (!success) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "이미 사용 중인 아이디입니다."
            );
            return "redirect:/signUp";
        }

        session.removeAttribute("requestedRole");

        redirectAttributes.addFlashAttribute(
                "message",
                "회원가입이 완료되었습니다. 로그인해주세요."
        );

        return "redirect:/LocalLogin";
    }

    // 학생 로그인
    @GetMapping("/login/route/student")
    public String studentLoginPage(HttpSession session) {
        session.setAttribute("requestedRole", "STUDENT");
        return "/login";
    }

    // 교사 로그인
    @GetMapping("/login/route/teacher")
    public String teacherLoginPage(HttpSession session) {
        session.setAttribute("requestedRole", "TEACHER");
        return "/login";
    }

    // 관리자 로그인
    @GetMapping("/login/route/admin")
    public String adminLoginPage(HttpSession session) {
        session.setAttribute("requestedRole", "ADMIN");
        return "/login";
    }

    // 일반 로그인
    @GetMapping("/LocalLogin")
    private String localLogin() {
        return "LocalLogin";
    }

    // 로그인 처리
    @PostMapping("/login")
    public String loginProcess(
            @RequestParam("loginId") String loginId,
            @RequestParam("passwordHash") String passwordHash,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        User loginUser = userService.login(loginId, passwordHash);

        if (loginUser == null) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "존재하지않는 회원입니다. 회원가입 후 로그인해주세요."
            );
            return "redirect:/signUp";
        } else if (loginUser.getStatus() == UserStatus.WITHDRAWN) {
            return "redirect:/";
        }

        User latestUser = userService.findByUserId(loginUser.getUserId());
        session.setAttribute("loginUser", latestUser);

        if (latestUser.getStatus() == UserStatus.PENDING) {
            if (latestUser.getRole() == UserRole.ADMIN) {
                return "redirect:/adminWaiting";
            } else {
                return "redirect:/blankPage";
            }
        } else if (latestUser.getStatus() == UserStatus.ACTIVE) {
            if (latestUser.getRole() == UserRole.STUDENT) {
                return "/main/studentMain";
            } else if (latestUser.getRole() == UserRole.TEACHER) {
                return "/main/teacherMain";
            } else if (latestUser.getRole() == UserRole.ADMIN) {
                return "main/adminMain";
            }
        }

        return "redirect:/";
    }

    // 학생, 교사 승인 대기 페이지
    @GetMapping("/blankPage")
    public String blankPage(HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");

        if (loginUser == null) {
            return "redirect:/";
        }

        if (loginUser.getStatus() == UserStatus.ACTIVE) {
            if (loginUser.getRole() == UserRole.ADMIN) {
                return "redirect:templates/main/adminMain";
            } else if (loginUser.getRole() == UserRole.STUDENT) {
                return "redirect:templates/main/studentMain";
            } else if (loginUser.getRole() == UserRole.TEACHER) {
                return "redirect:templates/main/teacherMain";
            }
        }

        return "main/blankPage";
    }

    // 로그아웃
    @GetMapping("/logout")
    public String logout(
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        redirectAttributes.addFlashAttribute(
                "message",
                "로그아웃되었습니다."
        );

        return "redirect:/";
    }

    // 회원 탈퇴
    @PostMapping("/user/withdraw")
    public String withdraw(
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        User loginUser = (User) session.getAttribute("loginUser");

        if (loginUser == null) {
            return "redirect:/login";
        }

        userService.withdrawUser(loginUser.getUserId());
        session.removeAttribute("loginUser");

        redirectAttributes.addFlashAttribute(
                "message",
                "회원 탈퇴가 완료되었습니다. 소중한 정보는 1년간 보관됩니다."
        );

        return "redirect:/";
    }
}

