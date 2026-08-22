package com.example.EduPOP.controller.auth;

import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.domain.user.UserRole;
import com.example.EduPOP.domain.user.UserStatus;
import com.example.EduPOP.service.auth.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

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
    public String signUpProcess(@ModelAttribute User user,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        String requestedRole = (String) session.getAttribute("requestedRole");
        if (requestedRole == null){
            requestedRole = "NONE";
        }
        user.setRole(UserRole.valueOf(requestedRole));

        boolean success = userService.registerLocalUser(user);

        if (!success) {
            // 중복 -> 회원가입 화면으로 이동 + 실패 메시지
            redirectAttributes.addFlashAttribute("error", "이미 사용 중인 아이디입니다.");
            return "redirect:/signUp";
        }


        session.removeAttribute("requestedRole");
        // 가입 성공 -> 로그인 화면으로 이동 + 성공 메시지
        redirectAttributes.addFlashAttribute("message", "회원가입이 완료되었습니다. 로그인해주세요.");
        return "redirect:/LocalLogin";
    }

    //역할 별 로그인 눌렀을 때 로그인 화면으로 이동
    //학생
    @GetMapping("/login/route/student")
    public String studentLoginPage(HttpSession session) {
        // 세션에 이 회원은 학생이라는 정보 남김
        session.setAttribute("requestedRole", "STUDENT");

        return "/login";
    }
    //교사
    @GetMapping("/login/route/teacher")
    public String teacherLoginPage(HttpSession session) {
        // 세션에 이 회원은 교사라는 정보 남김
        session.setAttribute("requestedRole", "TEACHER");

        return "/login";
    }
    //관리자
    @GetMapping("/login/route/admin")
    public String adminLoginPage(HttpSession session) {
        // 세션에 이 회원은 관리자라는 정보 남김
        session.setAttribute("requestedRole", "ADMIN");
        return "/login";
    }

    //일반 로그인
    @GetMapping("/LocalLogin")
    private String LocalLogin(){
        return "LocalLogin";
    }



    // 로그인 화면
    @PostMapping("/login")
    public String loginProcess(@RequestParam String login_id, @RequestParam String password_hash,
    HttpSession session, RedirectAttributes redirectAttributes) {

        User loginUser = userService.login(login_id, password_hash);
        //로그인 실패시
        if (loginUser == null) {
            redirectAttributes.addFlashAttribute("error", "존재하지않는 회원입니다. 회원가입 후 로그인해주세요.");
            return "redirect:/signUp";
        }

        //로그인 성공시 세션에 저장
        session.setAttribute("loginUser", loginUser);

        //로그인한 유저의 역할, 상태확인
        //관리자 버튼으로 로그인한 미등록된 유저라면
        if (loginUser.getStatus() == UserStatus.PENDING && loginUser.getRole() == UserRole.ADMIN) {
            //관리자 대기 페이지로
            return "redirect:/adminWaiting";
            //학생 버튼으로 로그인한 미등록 유저
        } else if (loginUser.getStatus()==UserStatus.PENDING && loginUser.getRole()==UserRole.STUDENT) {
            //승인대기 페이지
            return "redirect:/blankPage";
        } else if (loginUser.getStatus()==UserStatus.PENDING && loginUser.getRole()==UserRole.TEACHER) {
            return "redirect:/blankPage";

            //관리자가 승인한 상태 -> 역할 확인 후 해당 페이지로
        } else if (loginUser.getStatus() == UserStatus.ACTIVE) {
            if (loginUser.getRole() == UserRole.STUDENT) {
                return "/main/studentMain";
            } else if (loginUser.getRole() == UserRole.TEACHER) {
                return "/main/teacherMain";
            } else if (loginUser.getRole() == UserRole.ADMIN) {
                return "main/adminMain";
            }
        }
    return "redirect:/";
    }

    //학생, 교사 승인 대기 페이지
    @GetMapping("/blankPage")
    public String blankPage(){
        return "main/blankPage";
    }


    // 로그아웃
    @GetMapping("/logout")
    public String logout(HttpServletRequest request,
                         RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); // 세션 만료
        }
        redirectAttributes.addFlashAttribute("message", "로그아웃되었습니다.");
        return "redirect:/"; // 뷰 직접 반환 대신 redirect (PRG)
    }


}
