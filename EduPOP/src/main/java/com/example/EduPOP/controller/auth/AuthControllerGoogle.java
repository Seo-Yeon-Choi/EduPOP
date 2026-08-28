package com.example.EduPOP.controller.auth;

import com.example.EduPOP.config.SessionConst;
import com.example.EduPOP.domain.user.Academy;
import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.domain.user.UserRole;
import com.example.EduPOP.domain.user.UserStatus;
import com.example.EduPOP.service.auth.AcademyService;
import com.example.EduPOP.service.auth.GoogleService;
import com.example.EduPOP.service.auth.SecurityLoginService;
import com.example.EduPOP.service.auth.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
public class AuthControllerGoogle {

    private final GoogleService googleService;
    private final UserService userService;
    private final AcademyService academyService;
    private final SecurityLoginService securityLoginService;

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    @GetMapping("/google/login/student")
    public String googleStudentLogin(HttpSession session) {

        session.setAttribute("requestedRole", "STUDENT");

        return "redirect:/google/login";
    }

    @GetMapping("/google/login/teacher")
    public String googleTeacherLogin(HttpSession session) {

        session.setAttribute("requestedRole", "TEACHER");

        return "redirect:/google/login";
    }

    @GetMapping("/google/login/admin")
    public String googleAdminLogin(HttpSession session) {

        session.setAttribute("requestedRole", "ADMIN");

        return "redirect:/google/login";
    }

    @GetMapping("/google/login")
    public String googleLogin(HttpSession session) {

        String state = UUID.randomUUID().toString();

        session.setAttribute("googleOAuthState", state);

        String authorizationUrl =
                UriComponentsBuilder
                        .fromUriString(
                                "https://accounts.google.com/o/oauth2/v2/auth"
                        )
                        .queryParam("client_id", clientId)
                        .queryParam("redirect_uri", redirectUri)
                        .queryParam("response_type", "code")
                        .queryParam(
                                "scope",
                                "openid profile email"
                        )
                        .queryParam("state", state)
                        .queryParam("prompt", "select_account")
                        .build()
                        .encode()
                        .toUriString();

        return "redirect:" + authorizationUrl;
    }

    @GetMapping("/google/callback")
    public String googleCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            HttpServletRequest request,
            HttpServletResponse response,
            HttpSession session
    ) {

        if (error != null || code == null || state == null) {

            session.removeAttribute("googleOAuthState");
            session.removeAttribute("requestedRole");

            return "redirect:/LocalLogin?error=google";
        }

        String savedState =
                (String) session.getAttribute("googleOAuthState");

        session.removeAttribute("googleOAuthState");

        if (savedState == null || !savedState.equals(state)) {

            session.removeAttribute("requestedRole");

            return "redirect:/LocalLogin?error=google-state";
        }

        String requestedRole =
                (String) session.getAttribute(SessionConst.REQUESTED_ROLE);

        /*
         * 역할을 미리 선택하지 않고 Google 로그인을 누른 경우
         * 신규 소셜 회원으로 처리
         */
        if (requestedRole == null) {
            requestedRole = "NONE";
        }

        try {
            User googleUser = googleService.loginWithGoogle(
                    code,
                    UserRole.valueOf(requestedRole)
            );

            User latestUser =
                    userService.findByUserId(googleUser.getUserId());

            if (latestUser != null) {
                googleUser = latestUser;
            }

            // session.setAttribute(SessionConst.LOGIN_USER, googleUser);
            session.removeAttribute("requestedRole");

            // SpringSecurity 로그인
            securityLoginService.login(
                    googleUser,
                    request,
                    response
            );

            /*
             * 신규 소셜 회원이거나
             * 학생/선생님인데 학원이 선택되지 않은 경우
             */
            if (googleUser.getRole() == UserRole.NONE
                    || ((googleUser.getRole() == UserRole.STUDENT
                    || googleUser.getRole() == UserRole.TEACHER)
                    && googleUser.getAcademyId() == null)) {

                List<Academy> academies =
                        academyService.getAllAcademies();

                session.setAttribute("academies", academies);

                return "redirect:/selectAcademy";
            }

            if (googleUser.getStatus() == UserStatus.PENDING) {

                if (googleUser.getRole() == UserRole.ADMIN) {
                    return "redirect:/adminWaiting";
                }

                return "redirect:/blankPage";
            }

            if (googleUser.getStatus() == UserStatus.ACTIVE) {

                if (googleUser.getRole() == UserRole.STUDENT) {
                    return "redirect:/main/studentMain";
                }

                if (googleUser.getRole() == UserRole.TEACHER) {
                    return "redirect:/main/teacherMain";
                }

                if (googleUser.getRole() == UserRole.ADMIN) {
                    return "redirect:/main/adminMain";
                }
            }

            return "redirect:/";

        } catch (Exception e) {

            e.printStackTrace();

            session.removeAttribute("requestedRole");

            return "redirect:/LocalLogin?error=google";
        }
    }
}