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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthControllerKakao {
    private final KakaoService kakaoService;
    private final UserService userService;

    @GetMapping("/kakao/login/student")
    public String kakaoStudentLogin(HttpSession session) {
        session.setAttribute("requestedRole", "STUDENT"); // "너는 학생이야!" 기억
        return "redirect:/kakao/login"; // 실제 카카오 인증 주소로 토스
    }

    // 교사가 누르는 카카오 로그인 버튼
    @GetMapping("/kakao/login/teacher")
    public String kakaoTeacherLogin(HttpSession session) {
        session.setAttribute("requestedRole", "TEACHER"); // "너는 교사야!" 기억
        return "redirect:/kakao/login";
    }

    // 관리자가 누르는 카카오 로그인 버튼
    @GetMapping("/kakao/login/admin")
    public String kakaoAdminLogin(HttpSession session) {
        session.setAttribute("requestedRole", "ADMIN"); // "너는 관리자야!" 기억
        return "redirect:/kakao/login";
    }

    //카카오 로그인 버튼 누르면 카카오로 안내
    @GetMapping("/kakao/login")
    public String kakaoLogin() {
        //발급받은 키 삽입
        String kakaoAddress = "https://kauth.kakao.com/oauth/" +
                "authorize?client_id=f0d17d7cf78033e1ed7f979b9b09591b" +
                "&redirect_uri=http://localhost:8080/kakao/callback&response_type=code" +
                "&prompt=login";
        return "redirect:" + kakaoAddress;
    }

    //user가 인증코드 들고 옴
    @GetMapping("/kakao/callback")
    public String kakaoCallback(@RequestParam String code,
                                HttpSession session) {
        //카카오 로그인 완료 및 회원 정보 가져오기
        String requestedRole = (String) session.getAttribute("requestedRole");
        if (requestedRole == null) {
            requestedRole = "NONE"; //값이 없으면 NONE으로 기본값 설정
        }
        // 카카오 서비스로 로그인 처리할 때 이 역할(role)도 같이 넘겨줌
        User kakaoUser = kakaoService.loginWithKakao(code, UserRole.valueOf(requestedRole));

        User latestUser = userService.findById(kakaoUser.getUser_id());
        if (latestUser != null) {
            kakaoUser = latestUser; // 최신 정보로 덮어쓰기
        }
        //세션에 로그인 정보 저장
        session.setAttribute("loginUser", kakaoUser);

        //로그인한 유저의 역할, 상태확인
        //관리자 버튼으로 로그인한 미등록된 유저라면
        if (kakaoUser.getStatus() == UserStatus.PENDING) {
            if (kakaoUser.getRole() == UserRole.ADMIN) {
                return "redirect:/adminWaiting";
            } else {
                return "redirect:/blankPage"; // 학생, 교사 승인 대기 페이지
            }

            //관리자가 승인한 상태 -> 역할 확인 후 해당 페이지로
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

