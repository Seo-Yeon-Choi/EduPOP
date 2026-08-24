package com.example.EduPOP.controller.auth;

import com.example.EduPOP.domain.user.Academy;
import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.domain.user.UserRole;
import com.example.EduPOP.domain.user.UserStatus;
import com.example.EduPOP.repository.user.AcademyMapper;
import com.example.EduPOP.repository.user.UserMapper;
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

    private final AcademyMapper academyMapper;
    private final UserMapper userMapper;

    @GetMapping("/register-page")
    public String registerPage(){
        return "register-page";
    }

    @PostMapping("academy/register")
    public String registerAcademy(
    @RequestParam String name,
    @RequestParam String address,
    @RequestParam String phone,
    @RequestParam String business_cer,
    HttpSession session
    )
    {
        //DB저장
        Academy academy = new Academy();
        academy.setName(name);
        academy.setAddress(address);
        academy.setPhone(phone);
        academy.setBusiness_cer(business_cer);

        academyMapper.save(academy);

        //학원 등록 성공한 user의 role을 관리자로 업데이트
        //세션에서 로그인한 user정보 가져와서 role Admin, 상태 Active로변경
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser != null){
            loginUser.setRole(UserRole.ADMIN);
            loginUser.setStatus(UserStatus.ACTIVE);

        //DB에 있는 user role 업데이트
        userMapper.updateAcademyAndStatus(
                loginUser.getUser_id(),
                academy.getAcademy_id(),
                UserRole.ADMIN,
                UserStatus.ACTIVE);

        }
        return "redirect:/adminMain";
    }

    @GetMapping("/adminMain")
    public String adminPage() {
        return "/main/adminMain";
    }

}
