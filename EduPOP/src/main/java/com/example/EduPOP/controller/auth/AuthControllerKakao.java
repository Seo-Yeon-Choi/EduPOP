package com.example.EduPOP.controller.auth;

import com.example.EduPOP.domain.user.Academy;
import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.domain.user.UserRole;
import com.example.EduPOP.domain.user.UserStatus;
import com.example.EduPOP.service.auth.AcademyService;
import com.example.EduPOP.service.auth.KakaoService;
import com.example.EduPOP.service.auth.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AuthControllerKakao {

    private final KakaoService kakaoService;
    private final UserService userService;
    private final AcademyService academyService;

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
    ) { //유저가 요청한 역할을 세션에 넣음
        String requestedRole = (String) session.getAttribute("requestedRole");
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

        //userId가 있으면 kakaoUser에
        if (latestUser != null) {
            kakaoUser = latestUser;
        }
        //userId가 없으면 세션에 kakaoUser 지정
        session.setAttribute("loginUser", kakaoUser);

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

