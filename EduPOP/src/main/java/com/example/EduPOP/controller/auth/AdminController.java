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

    // 관리자 전체회원 관리 페이지
    // 관리자 메인 페이지 이동
    @GetMapping("/main/adminMain")
    public String adminMain() {
        return "main/adminMain"; // 실제 파일 위치: src/main/resources/templates/main/adminMain.html
    }
    @GetMapping("/admin/users")
    public String adminPage(Model model) {
        // PENDING 유저 목록을 서비스에서 가져옴
        List<User> pendingUsers = userService.getPendingUsers();
        List<User> allUsers = userService.getAllUsersExceptAdmin();

        // 모델에 담아서 HTML로 보냄
        model.addAttribute("pendingUsers", pendingUsers);
        model.addAttribute("allUsers", allUsers);

        return "/admin/users"; // 관리자 전체회원 관리 페이지
    }
    //관리자 회원 상태 수정
    @PostMapping("/admin/updateStatus")
    public String updateStatus(@RequestParam("user_id") Long user_id,
                               @RequestParam("status") UserStatus status,
                               RedirectAttributes redirectAttributes) {
        userService.updateUserStatusByAdmin(user_id, status);
        redirectAttributes.addFlashAttribute("message", "회원 상태가 수정되었습니다.");
        return "redirect:/main/adminMain";
    }

    // 승인 버튼을 눌렀을 때 실행
    @GetMapping("/admin/approve")
    public String approveUser(@RequestParam("user_id") Long user_id) {
        // 유저 상태를 ACTIVE로 변경
        userService.approveUser(user_id);
        return "redirect:/main/adminMain";
    }
    // 거절 버튼을 눌렀을 때 실행
    @GetMapping("/admin/reject")
    public String rejectUser(@RequestParam("user_id") Long user_id, RedirectAttributes redirectAttributes) {
        // 상태를 바꾸지 않고 그대로 PENDING 유지
        // '거절됨' 메시지 전달
        redirectAttributes.addFlashAttribute("message", "가입 승인이 거절되었습니다.");
        return "redirect:/main/adminMain";
    }

    // 선택된 회원들 일괄 상태 변경 처리
    @PostMapping("/admin/updateStatusBatch")
    public String updateStatusBatch(@RequestParam(value = "userIds", required = false) List<Long> userIds,
                                    @RequestParam("status") UserStatus status,
                                    RedirectAttributes redirectAttributes) {
        if (userIds == null || userIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "선택된 회원이 없습니다.");
            return "redirect:/main/adminMain";
        }

        // 서비스 호출해서 한 번에 상태 변경
        userService.updateUsersStatusBatch(userIds, status);
        redirectAttributes.addFlashAttribute("message", "선택한 회원들의 상태가 일괄 변경되었습니다.");

        return "redirect:/main/adminMain";
    }

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


    @GetMapping("/admin/classes")
    public String classes(){
        return "admin/classes";
    }
}
