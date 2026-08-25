package com.example.EduPOP.controller.auth;

import com.example.EduPOP.domain.user.Academy;
import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.domain.user.UserRole;
import com.example.EduPOP.domain.user.UserStatus;
import com.example.EduPOP.service.auth.AcademyService;
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

    private  final AcademyService academyService;
    private final UserService userService;

    // 기본 도메인 요청 시 로그인 페이지로 이동
    @GetMapping("/")
    public String home() {
        return "/mainHomePage";
    }

    // 회원가입 화면
    @GetMapping("/signUp")
    public String signUpPage(Model model) {
        List<Academy> academies = academyService.getAllAcademies();
        model.addAttribute("academies", academies);
        return "signUp";
    }

    // 회원가입 처리
    @PostMapping("/signUp")
    public String signUpProcess(
            @ModelAttribute User user,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {//세션에 요청받은 역할 넣음
        String requestedRole = (String) session.getAttribute("requestedRole");
        // 요청이 없다면 역할 기본값 none
        if (requestedRole == null) {
            requestedRole = "NONE";
        }
        // 요청이 있다면 회원을 역할을 요청받은 역할로 지정
        user.setRole(UserRole.valueOf(requestedRole));

        //회원 저장 성공여부
        boolean success = userService.registerLocalUser(user);
        //중복으로 저장에 실패했을시
        if (!success) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "이미 사용 중인 아이디입니다."
            );
            return "redirect:/signUp";
        }
        // 저장 성공시 요청받은 역할 세션 날림
        session.removeAttribute("requestedRole");
        // 저장 성공 시 가입 완료
        redirectAttributes.addFlashAttribute(
                "message",
                "회원가입이 완료되었습니다. 로그인해주세요."
        );

        return "redirect:/LocalLogin";
    }
//--------------------------------------------------------------------------------------------------
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

//--------------------------------------------------------------------------------------
    // 학생, 교사 승인 대기 페이지
    @GetMapping("/blankPage")
    public String blankPage(HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");

        if (loginUser == null) {
            return "redirect:/";
        }

        if (loginUser.getStatus() == UserStatus.ACTIVE) {
            if (loginUser.getRole() == UserRole.ADMIN) {
                return "redirect:main/adminMain";
            } else if (loginUser.getRole() == UserRole.STUDENT) {
                return "redirect:main/studentMain";
            } else if (loginUser.getRole() == UserRole.TEACHER) {
                return "redirect:main/teacherMain";
            }
        }

        return "main/blankPage";
    }
//--------------------------------------------------------------------------------
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
//--------------------------------------------------------------------------------------
    //카카오 회원 로그인 후 학원 선택
    @GetMapping("/selectAcademy")
    public String selectAcademy(Model model){
        //모든 학원을 가져와서 model에 담음
        List<Academy> academies = academyService.getAllAcademies();
        model.addAttribute("academies", academies);
        return "/selectAcademy";
    }

    //카카오 회원이 학원 선택 후
    @PostMapping("/selectAcademy")
    public String selectAcademy(@RequestParam Long academyId,
                                HttpSession session){
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null){
            return "redirect:/";
        }

        //로그인한 유저의 학원 번호 DB 업데이트
        userService.updateAcademyId(
                loginUser.getUserId(),
                academyId
        );
        //로그인한 유저의 학원 번호 세션 업데이트
        loginUser.setAcademyId(academyId);
        session.setAttribute("loginUser", loginUser);

        // PENDING 상태
        if (loginUser.getStatus() == UserStatus.PENDING) {
            if (loginUser.getRole() == UserRole.ADMIN) {
                return "redirect:/adminWaiting";
            }
            return "redirect:/blankPage";
        }


        // ACTIVE 상태
        if (loginUser.getStatus() == UserStatus.ACTIVE) {
            if (loginUser.getRole() == UserRole.STUDENT) {
                return "redirect:main/studentMain";
            }
            if (loginUser.getRole() == UserRole.TEACHER) {
                return "redirect:main/teacherMain";
            }
            if (loginUser.getRole() == UserRole.ADMIN) {
                return "redirect:main/adminMain";
            }
        }

        return "redirect:/";
    }

    @GetMapping("/main/studentMain")
    public String studentMain(){
        return "main/studentMain";
    }

    @GetMapping("/main/teacherMain")
    public String teacherMain(){
        return "main/teacherMain";
    }

}

