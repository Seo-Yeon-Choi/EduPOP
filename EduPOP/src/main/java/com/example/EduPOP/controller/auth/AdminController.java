 package com.example.EduPOP.controller.auth;

import com.example.EduPOP.domain.user.*;
import com.example.EduPOP.service.auth.AcademyService;
import com.example.EduPOP.service.auth.ClassService;
import com.example.EduPOP.service.auth.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

//관리자: 학원 등록, 회원관리(조회,수정,삭제), 학원관리(수정,탈퇴), 반 관리
@Controller
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final AcademyService academyService;
    private final ClassService classService;

    //관리자 학원등록버튼
    @GetMapping("/adminWaiting")
    public String adminWaiting() {
        return "main/adminWaiting";
    }
//-------------------------------------------------------------------------
    // 관리자 회원 관리 페이지
    // 관리자 메인 페이지 이동
    @GetMapping("/main/adminMain")
    public String adminMain() {
        return "main/adminMain";
    }

    //회원 관리
    // 회원 관리 페이지로 이동
    @GetMapping("/admin/users")
    public String adminPage(HttpSession session,
                            Model model) {
        User loginUser = (User) session.getAttribute("loginUser");
       //로그인이 안되어있거나 academyId가 없다면 돌려보냄
        if (loginUser == null || loginUser.getAcademyId() == null){
            return "redirect:/";
        }
        //로그인한 유저의 academyId를 기준으로 조회
        Long academyId = loginUser.getAcademyId();

        List<User> academyUsers = userService.getUsersAcademyId(academyId);

        model.addAttribute("allUsers", academyUsers);
        return "/admin/users";
    }

    //관리자가 표에서 회원 개별 상태 수정
    @PostMapping("/admin/updateStatus")
    public String updateStatus(
            @RequestParam("userId") Long userId,
            @RequestParam("status") UserStatus status
    ) {
        userService.updateStatus(userId, status);
        return "redirect:/admin/users";
    }

    // 회원선택 후 일괄 상태 변경 처리
    @PostMapping("/admin/updateUsersStatusBatch")
    public String updateUsersStatusBatch(
            @RequestParam(value = "userIds", required = false) List<Long> userIds,
            @RequestParam("status") UserStatus status
    ) {
        userService.updateUsersStatusBatch(userIds, status);

        return "redirect:/admin/users";
    }
//-------------------------------------------------------------------------
    //학원 관리
    //학원 조회
    @GetMapping("/admin/academies")
    public String findAllAcademies(HttpSession session, Model model) {

        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "redirect:/";
        }

        Long academyId = loginUser.getAcademyId();
        if (academyId == null) {
            return "redirect:/";
        }

        Academy academy = academyService.getAcademyById(academyId);
        model.addAttribute("academy", academy);
        return "/admin/academies";
    }

    //학원 수정
    @PostMapping("/admin/updateAcademy")
    public String updateAcademy(
            @ModelAttribute Academy academy,
            HttpSession session
    ) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null || loginUser.getAcademyId() == null) {
            return "redirect:/";
        }

        academy.setAcademyId(loginUser.getAcademyId());
        academyService.updateAcademy(academy);
        return "redirect:/admin/academies";
    }

    //학원 삭제
    @PostMapping("/admin/deleteAcademy")
    public String deleteAcademy(HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null || loginUser.getAcademyId() == null) {
            return "redirect:/";
        }

        academyService.deleteAcademy(loginUser.getAcademyId());
        session.invalidate();
        return "redirect:/";
    }
//--------------------------------------------------------------------------
    //학급 조회
    @GetMapping("/admin/classes")
    public String classes(HttpSession session,
                          Model model){
        User loginUser = (User) session.getAttribute("loginUser");

        System.out.println("로그인 유저 = " + loginUser);

        if(loginUser == null || loginUser.getAcademyId() == null){
            return "redirect:/";
        }

        System.out.println("관리자 academyId = " + loginUser.getAcademyId());

        if (loginUser.getAcademyId() == null) {
            System.out.println("academyId가 null");
            return "redirect:/";
        }

        Long academyId = loginUser.getAcademyId();
        List<AcademyClass> academyClasses = classService.findClassesByAcademyId(academyId);

        System.out.println("조회된 학급 수 = " + academyClasses.size());
        System.out.println("조회된 학급 = " + academyClasses);

        model.addAttribute("academyClasses", academyClasses);
        return "admin/classes";
    }

    //학급 상세 조회
    @GetMapping("/admin/classes/{classId}")
    public String classDetail(
            @PathVariable Long classId,
            HttpSession session,
            Model model){
        User loginUser = (User) session.getAttribute("loginUser");
        if(loginUser == null || loginUser.getAcademyId() == null){
            return "redirect:/";
        }
        Long academyId = loginUser.getAcademyId();
        ClassDetail classDetail = classService.findClassById(classId,academyId);
        if (classDetail == null){
            return "redirect:/admin/classes";
        }
        model.addAttribute("classDetail",classDetail);
        return "admin/classDetail";
    }

    //학급 수정
    @PostMapping("/admin/updateClass")
    public String updateClass(@ModelAttribute AcademyClass classInfo,
                              HttpSession session){
        User loginUser =
                (User) session.getAttribute("loginUser");
        if (loginUser == null ||
                loginUser.getAcademyId() == null) {
            return "redirect:/";
        }

        classInfo.setAcademyId(loginUser.getAcademyId());
        classService.updateClass(classInfo);

        return "redirect:/admin/classes/"
                + classInfo.getClassId();
    }


    // 학급 삭제
    @PostMapping("/admin/deleteClass")
    public String deleteClass(
            @RequestParam Long classId,
            HttpSession session
    ) {
        User loginUser =
                (User) session.getAttribute("loginUser");
        if (loginUser == null ||
                loginUser.getAcademyId() == null) {
            return "redirect:/";
        }

        classService.deleteClass(
                classId,
                loginUser.getAcademyId()
        );

        return "redirect:/admin/classes";
    }
}
