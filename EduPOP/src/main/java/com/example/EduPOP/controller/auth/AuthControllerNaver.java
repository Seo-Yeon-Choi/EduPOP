package com.example.EduPOP.controller.auth;

import com.example.EduPOP.config.SessionConst;
import com.example.EduPOP.domain.user.Academy;
import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.domain.user.UserRole;
import com.example.EduPOP.domain.user.UserStatus;
import com.example.EduPOP.service.auth.AcademyService;
import com.example.EduPOP.service.auth.NaverService;
import com.example.EduPOP.service.auth.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class AuthControllerNaver {

    private final NaverService naverService;
    private final UserService userService;
    private final AcademyService academyService;

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.redirect-uri}")
    private String redirectUri;

    @GetMapping("/naver/login/student")
    public String naverStudentLogin(HttpSession session) {
        session.setAttribute("requestedRole", "STUDENT");
        return "redirect:/naver/login";
    }

    @GetMapping("/naver/login/teacher")
    public String naverTeacherLogin(HttpSession session) {
        session.setAttribute("requestedRole", "TEACHER");
        return "redirect:/naver/login";
    }

    @GetMapping("/naver/login/admin")
    public String naverAdminLogin(HttpSession session) {
        session.setAttribute("requestedRole", "ADMIN");
        return "redirect:/naver/login";
    }

    @GetMapping("/naver/login")
    public String naverLogin(HttpSession session) {
        String state = UUID.randomUUID().toString();
        session.setAttribute("naverOAuthState", state);

        String naverAddress = UriComponentsBuilder
                .fromUriString("https://nid.naver.com/oauth2.0/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", state)
                .build()
                .encode()
                .toUriString();

        return "redirect:" + naverAddress;
    }

    @GetMapping("/naver/callback")
    public String naverCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            HttpSession session
    ) {
        if (error != null || code == null || state == null) {
            return "redirect:/login?error=naver";
        }

        String savedState = (String) session.getAttribute("naverOAuthState");
        session.removeAttribute("naverOAuthState");

        if (savedState == null || !savedState.equals(state)) {
            return "redirect:/login?error=naver-state";
        }

        String requestedRole = (String) session.getAttribute(SessionConst.REQUESTED_ROLE);
        if (requestedRole == null) {
            requestedRole = "NONE";
        }

        try {
            User naverUser = naverService.loginWithNaver(
                    code,
                    state,
                    UserRole.valueOf(requestedRole)
            );

            User latestUser = userService.findByUserId(naverUser.getUserId());
            if (latestUser != null) {
                naverUser = latestUser;
            }

            session.setAttribute(SessionConst.LOGIN_USER, naverUser);
            session.removeAttribute("requestedRole");

            if ((naverUser.getRole() == UserRole.STUDENT
                    || naverUser.getRole() == UserRole.TEACHER)
                    && naverUser.getAcademyId() == null) {
                List<Academy> academies = academyService.getAllAcademies();
                session.setAttribute("academies", academies);
                return "redirect:/selectAcademy";
            }

            if (naverUser.getStatus() == UserStatus.PENDING) {
                if (naverUser.getRole() == UserRole.ADMIN) {
                    return "redirect:/adminWaiting";
                }
                return "redirect:/blankPage";
            }

            if (naverUser.getStatus() == UserStatus.ACTIVE) {
                if (naverUser.getRole() == UserRole.STUDENT) {
                    return "redirect:/main/studentMain";
                }
                if (naverUser.getRole() == UserRole.TEACHER) {
                    return "redirect:/main/teacherMain";
                }
                if (naverUser.getRole() == UserRole.ADMIN) {
                    return "redirect:/main/adminMain";
                }
            }

            return "redirect:/";
        } catch (RuntimeException e) {
            session.removeAttribute("requestedRole");
            return "redirect:/login?error=naver";
        }
    }
}