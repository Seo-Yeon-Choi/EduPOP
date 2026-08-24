package com.example.EduPOP.controller.auth;

import com.example.EduPOP.domain.user.Academy;
import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.domain.user.UserRole;
import com.example.EduPOP.domain.user.UserStatus;
import com.example.EduPOP.repository.user.AcademyMapper;
import com.example.EduPOP.repository.user.UserMapper;
import com.example.EduPOP.service.auth.AcademyService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

//학원 등록
@Controller
@RequiredArgsConstructor
public class AcademyController {

    private final AcademyService academyService;

    @GetMapping("/register-page")
    public String registerPage() {
        return "register-page";
    }

    //학원 등록 처리
    @PostMapping("/academy/register")
    public String registerAcademy(
            @RequestParam String name,
            @RequestParam String address,
            @RequestParam String phone,
            @RequestParam String businessCer,
            HttpSession session
    ) {
        //학원 등록 성공한 user의 role을 관리자로 업데이트
        //세션에서 로그인한 user정보 가져와서 role Admin, 상태 Active로 변경
        User loginUser = (User) session.getAttribute("loginUser");

        if (loginUser == null) {
            return "redirect:/login"; // 로그인이 풀려있다면 로그인 페이지로
        }

        //DB저장
        Academy academy = new Academy();
        academy.setName(name);
        academy.setAddress(address);
        academy.setPhone(phone);
        academy.setBusinessCer(businessCer);

        // 학원 저장 + 관리자 상태를 ACTIVE로 변경
        academyService.registerAcademy(academy, loginUser.getUserId());

        // DB에서 상태가 ACTIVE로 바뀐 최신 유저 정보를 다시 가져옴
        User updatedUser = academyService.findById(loginUser.getUserId());

        // 세션에 들어있던 user 정보를 최신 정보로 갱신
        session.setAttribute("loginUser", updatedUser);

        return "redirect:/main/adminMain";
    }

    //관리자 메인 페이지
    @GetMapping("/adminMain")
    public String adminPage() {
        return "main/adminMain";
    }
}
