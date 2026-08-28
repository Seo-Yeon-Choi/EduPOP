package com.example.EduPOP.controller.auth;

import com.example.EduPOP.config.SessionConst;
import com.example.EduPOP.domain.user.Academy;
import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.domain.user.UserRole;
import com.example.EduPOP.domain.user.UserStatus;
import com.example.EduPOP.service.auth.AcademyService;
import com.example.EduPOP.service.auth.KakaoService;
import com.example.EduPOP.service.auth.UserService;
import jakarta.servlet.http.HttpServletRequest;
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
public class AuthControllerKakao {

    private final KakaoService kakaoService;
    private final UserService userService;
    private final AcademyService academyService;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

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
    public String kakaoLogin(HttpSession session) {

        String state = UUID.randomUUID().toString();

        session.setAttribute("kakaoOAuthState", state);

        String authorizationUrl =
                UriComponentsBuilder
                        .fromUriString("https://kauth.kakao.com/oauth/authorize")
                        .queryParam("client_id", clientId)
                        .queryParam("redirect_uri", redirectUri)
                        .queryParam("response_type", "code")
                        .queryParam("state", state)
                        .queryParam("prompt", "login")
                        .build()
                        .encode()
                        .toUriString();

        return "redirect:" + authorizationUrl;
    }

    // user가 인증코드 들고 옴
    @GetMapping("/kakao/callback")
    public String kakaoCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            HttpServletRequest request,
            HttpSession session
    ) {
        // 카카오 로그인 실패 또는 필수값 누락 확인
        if (error != null || code == null || state == null) {
            session.removeAttribute("kakaoOAuthState");
            session.removeAttribute("requestedRole");

            return "redirect:/LocalLogin?error=kakao";
        }

        // 세션에 저장해둔 OAuth State 가져오기
        String savedState = (String) session.getAttribute("kakaoOAuthState");

        // state는 한 번만 사용
        session.removeAttribute("kakaoOAuthState");

        // OAuth state 검증
        if (savedState == null
                || !savedState.equals(state)) {

            session.removeAttribute("requestedRole");

            return "redirect:/LocalLogin?error=kakao-state";
        }
        //유저가 요청한 역할을 세션에 넣음
        String requestedRole = (String) session.getAttribute(SessionConst.REQUESTED_ROLE);

        //요청 역할이 없으면 none 기본값 설정
        if (requestedRole == null) {
            requestedRole = "NONE";
        }
        // 카카오로 로그인한 User에 code와 요청한 역할 넣음
        User kakaoUser = kakaoService.loginWithKakao(
                code,
                UserRole.valueOf(requestedRole)
        );
        //userId로 kakao회원을 조회
        User latestUser = userService.findByUserId(kakaoUser.getUserId());

        if(latestUser != null){
            kakaoUser = latestUser;
        }
        // 역할 선택값 삭제
        session.removeAttribute("requestedRole");

        // 로그인 성공 후 세션 ID 변경
        request.changeSessionId();

        // 로그인 사용자 세션 저장
        session.setAttribute(
                SessionConst.LOGIN_USER,
                kakaoUser
        );

        //역할이 학생,교사이면서 학원Id가 없으면 학원 선택 페이지로 보냄
        if ((kakaoUser.getRole() == UserRole.STUDENT
                || kakaoUser.getRole() == UserRole.TEACHER)
                && kakaoUser.getAcademyId() == null ){
            List<Academy> academies = academyService.getAllAcademies();
            session.setAttribute("academies", academies);
            return "redirect:/selectAcademy";
        }

        // 상태가 PENDING일 때
        if (kakaoUser.getStatus() == UserStatus.PENDING) {
            //ADMIN이면 관리자 대기화면
            if (kakaoUser.getRole() == UserRole.ADMIN) {
                return "redirect:/adminWaiting";
                //ADMIN아니면 일반 대기화면
            } else {
                return "redirect:/blankPage";
            }
            //상태가 ACTIVE일 때
        } else if (kakaoUser.getStatus() == UserStatus.ACTIVE) {
            //학생이면 학생메인 페이지
            if (kakaoUser.getRole() == UserRole.STUDENT) {
                return "redirect:/main/studentMain";
            } else if (kakaoUser.getRole() == UserRole.TEACHER) {
                return "redirect:/main/teacherMain";
            } else if (kakaoUser.getRole() == UserRole.ADMIN) {
                return "redirect:/main/adminMain";
            }
        }

        return "redirect:/";
    }
}