package com.example.EduPOP.controller.auth;

import com.example.EduPOP.domain.user.Academy;
import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.domain.user.UserStatus;
import com.example.EduPOP.repository.user.AcademyMapper;
import com.example.EduPOP.service.auth.AcademyService;
import com.example.EduPOP.service.auth.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

//관리자: 학원 등록, 회원관리(조회,수정,삭제), 학원관리(수정,탈퇴), 반 관리

@Controller
@RequiredArgsConstructor

public class AdminController {
private final UserService userService;
private final AcademyService academyService;
private final AcademyMapper academyMapper;

    //관리자 학원등록버튼
    @GetMapping("/adminWaiting")
    public String adminWaiting(){
        return "main/adminWaiting";
    }
//-------------------------------------------------------------------------------------------------------------------

    // 관리자 전체회원 관리 페이지
    // 관리자 메인 페이지 이동
    @GetMapping("/main/adminMain")
    public String adminMain() {
        return "main/adminMain";
    }
//-------------------------------------------------------------------------------------------------------------------

    // 회원 관리 페이지로 이동
    @GetMapping("/admin/users")
    public String adminPage(Model model) {
        //유저 목록을 서비스에서 가져옴
        List<User> allUsers = userService.getAllUsersExceptAdmin();

        // 모델에 담아서 HTML로 보냄
        model.addAttribute("allUsers", allUsers);
        return "/admin/users"; // 관리자 전체회원 관리 페이지
    }

    //관리자가 표에서 회원 개별 상태 수정
    @PostMapping("/admin/updateStatus")
    public String updateStatus(@RequestParam("user_id") Long user_id,
                               @RequestParam("status") UserStatus status
                               ) {
        userService.updateStatus(user_id, status);
        return "redirect:/admin/users";
    }

    // 회원선택 후 일괄 상태 변경 처리
    @PostMapping("/admin/updateUsersStatusBatch")
    //개별 상태 변경
    public String updateUsersStatusBatch(@RequestParam(value = "userIds", required = false)
                                        List<Long> userIds,
                                    @RequestParam("status") UserStatus status) {
        // 한 번에 상태 변경
        userService.updateUsersStatusBatch(userIds, status);

        return "redirect:/admin/users";
    }
//-------------------------------------------------------------------------------------------------------------------

    //전체 학원 조회
    @GetMapping("/admin/academies")
    public String findAllAcademies(Model model){
        List<Academy> academies = academyService.getAllAcademies();
        model.addAttribute("academies", academies);
        return "/admin/academies";
    }
    //학원 삭제
    @PostMapping("/admin/deleteAcademy")
    public String deleteAcademy(@RequestParam("academy_id") Long academy_id, RedirectAttributes redirectAttributes) {
        academyService.deleteAcademy(academy_id);
        redirectAttributes.addFlashAttribute("message", "학원 정보가 삭제되었습니다.");
        return "redirect:/admin/academies";
    }
    //학원 수정
    @PostMapping("/admin/updateAcademy")
    public String updateAcademy(@ModelAttribute Academy academy, RedirectAttributes redirectAttributes) {
        // 서비스 호출
        academyService.updateAcademy(
                academy.getAcademy_id(),
                academy.getName(),
                academy.getAddress(),
                academy.getPhone(),
                academy.getBusiness_cer()
        );
        redirectAttributes.addFlashAttribute("message","회원 정보가 수정되었습니다.");
        return "redirect:/admin/academies";
    }

//-------------------------------------------------------------------------------------------------------------------
    //반 수정
    @GetMapping("/admin/classes")
    public String classes(){
        return "admin/classes";
    }
}
