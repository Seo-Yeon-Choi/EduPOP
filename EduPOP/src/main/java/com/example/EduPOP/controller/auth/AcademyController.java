package com.example.EduPOP.controller.auth;

import com.example.EduPOP.config.SessionConst;
import com.example.EduPOP.domain.user.Academy;
import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.domain.user.UserRole;
import com.example.EduPOP.domain.user.UserStatus;
import com.example.EduPOP.repository.user.AcademyMapper;
import com.example.EduPOP.repository.user.UserMapper;
import com.example.EduPOP.service.auth.AcademyService;
import com.example.EduPOP.service.auth.SecurityLoginService;
import com.example.EduPOP.service.business.BusinessVerificationService;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

//학원 등록
@Controller
@RequiredArgsConstructor
public class AcademyController {

    private final AcademyService academyService;
    private final BusinessVerificationService businessVerificationService;
    private final SecurityLoginService securityLoginService;

    @GetMapping("/register-page")
    public String registerPage() {
        return "register-page";
    }

    //학원 등록 처리
// 학원 등록 처리
    @PostMapping("/academy/register")
    public String registerAcademy(
            @RequestParam String name,
            @RequestParam String address,
            @RequestParam String phone,
            @RequestParam String businessNumber,
            @RequestParam String representativeName,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate businessStartDate,
            HttpServletRequest request,
            HttpServletResponse response,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {

        // 로그인 사용자 확인
        User loginUser =
                (User) session.getAttribute(SessionConst.LOGIN_USER);

        if (loginUser == null) {
            return "redirect:/LocalLogin";
        }

        // 사업자등록번호에서 하이픈 제거
        String normalizedBusinessNumber =
                businessNumber.replaceAll("[^0-9]", "");

        // 사업자등록번호 형식 확인
        if (normalizedBusinessNumber.length() != 10) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "사업자등록번호는 10자리 숫자로 입력해주세요."
            );

            return "redirect:/register-page";
        }

        // 대표자명 확인
        if (representativeName == null
                || representativeName.trim().isEmpty()) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "대표자명을 입력해주세요."
            );

            return "redirect:/register-page";
        }

        // 개업일자 확인
        if (businessStartDate == null) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "개업일자를 입력해주세요."
            );

            return "redirect:/register-page";
        }

        try {

            // 국세청 API
            // 1. 사업자 정보 진위확인
            // 2. 현재 계속사업자인지 확인
            boolean verified =
                    businessVerificationService.verify(
                            normalizedBusinessNumber,
                            representativeName.trim(),
                            businessStartDate
                    );

            if (!verified) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "사업자등록정보가 일치하지 않거나 현재 운영 중인 사업자가 아닙니다."
                );

                return "redirect:/register-page";
            }

        } catch (Exception e) {

            // API 장애와 '정보 불일치'를 구분
            redirectAttributes.addFlashAttribute(
                    "error",
                    "사업자등록정보 확인 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
            );

            return "redirect:/register-page";
        }

        // 검증 성공 후 학원 생성
        Academy academy = new Academy();

        academy.setName(name.trim());
        academy.setAddress(address.trim());
        academy.setPhone(phone.trim());

        academy.setBusinessNumber(
                normalizedBusinessNumber
        );

        academy.setRepresentativeName(
                representativeName.trim()
        );

        academy.setBusinessStartDate(
                businessStartDate
        );

        // 학원 저장 + 관리자 상태 ACTIVE 변경
        academyService.registerAcademy(
                academy,
                loginUser.getUserId()
        );

        // DB에서 최신 사용자 정보 다시 조회
        User updatedUser =
                academyService.findById(
                        loginUser.getUserId()
                );

        // Spring Security 인증/권한 정보까지 다시 갱신
        securityLoginService.login(
                updatedUser,
                request,
                response
        );

        redirectAttributes.addFlashAttribute(
                "message",
                "사업자등록정보 확인이 완료되었습니다. 학원이 등록되었습니다."
        );

        return "redirect:/main/adminMain";
    }

    //관리자 메인 페이지
    @GetMapping("/adminMain")
    public String adminPage() {
        return "main/adminMain";
    }


}