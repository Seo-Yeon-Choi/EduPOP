package com.example.EduPOP.controller.auth;

import com.example.EduPOP.config.SessionConst;
import com.example.EduPOP.domain.user.Academy;
import com.example.EduPOP.domain.user.AcademyClass;
import com.example.EduPOP.domain.user.ClassDetail;
import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.domain.user.UserRole;
import com.example.EduPOP.domain.user.UserStatus;
import com.example.EduPOP.service.auth.AcademyService;
import com.example.EduPOP.service.auth.ClassauthService;
import com.example.EduPOP.service.auth.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

// 관리자: 대시보드, 회원 관리, 학원 관리, 반 관리
@Controller
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final AcademyService academyService;
    private final ClassauthService classauthService;

    @GetMapping("/adminWaiting")
    public String adminWaiting() {
        return "main/adminWaiting";
    }

    @GetMapping("/main/adminMain")
    public String adminMain(HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);

        if (loginUser == null) {
            return "redirect:/LocalLogin";
        }

        if (loginUser.getAcademyId() == null) {
            return "redirect:/adminWaiting";
        }

        Long academyId = loginUser.getAcademyId();
        List<User> academyUsers = userService.getUsersAcademyId(academyId);
        List<AcademyClass> academyClasses = classauthService.findClassesByAcademyId(academyId);
        Academy academy = academyService.getAcademyById(academyId);

        long studentCount = academyUsers.stream()
                .filter(user -> user.getRole() == UserRole.STUDENT)
                .count();
        long teacherCount = academyUsers.stream()
                .filter(user -> user.getRole() == UserRole.TEACHER)
                .count();
        long pendingCount = academyUsers.stream()
                .filter(user -> user.getStatus() == UserStatus.PENDING)
                .count();
        long inactiveCount = academyUsers.stream()
                .filter(user -> user.getStatus() == UserStatus.INACTIVE)
                .count();

        model.addAttribute("academy", academy);
        model.addAttribute("totalUsers", academyUsers.size());
        model.addAttribute("studentCount", studentCount);
        model.addAttribute("teacherCount", teacherCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("inactiveCount", inactiveCount);
        model.addAttribute("totalClasses", academyClasses.size());

        return "main/adminMain";
    }

    @GetMapping("/admin/users")
    public String adminPage(HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);

        if (loginUser == null || loginUser.getAcademyId() == null) {
            return "redirect:/";
        }

        Long academyId = loginUser.getAcademyId();
        List<User> academyUsers = userService.getUsersAcademyId(academyId);
        long pendingCount = academyUsers.stream()
                .filter(user -> user.getStatus() == UserStatus.PENDING)
                .count();

        model.addAttribute("allUsers", academyUsers);
        model.addAttribute("pendingCount", pendingCount);
        return "/admin/users";
    }

    @PostMapping("/admin/updateStatus")
    public String updateStatus(
            @RequestParam("userId") Long userId,
            @RequestParam("status") UserStatus status
    ) {
        userService.updateStatus(userId, status);
        return "redirect:/admin/users";
    }

    @PostMapping("/admin/updateUsersStatusBatch")
    public String updateUsersStatusBatch(
            @RequestParam(value = "userIds", required = false) List<Long> userIds,
            @RequestParam("status") UserStatus status
    ) {
        userService.updateUsersStatusBatch(userIds, status);
        return "redirect:/admin/users";
    }

    @GetMapping("/admin/academies")
    public String findAllAcademies(HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);

        if (loginUser == null || loginUser.getAcademyId() == null) {
            return "redirect:/";
        }

        Academy academy = academyService.getAcademyById(loginUser.getAcademyId());
        model.addAttribute("academy", academy);
        return "/admin/academies";
    }

    @PostMapping("/admin/updateAcademy")
    public String updateAcademy(
            @ModelAttribute Academy academy,
            HttpSession session
    ) {
        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);

        if (loginUser == null || loginUser.getAcademyId() == null) {
            return "redirect:/";
        }

        academy.setAcademyId(loginUser.getAcademyId());
        academyService.updateAcademy(academy);
        return "redirect:/admin/academies";
    }

    @PostMapping("/admin/deleteAcademy")
    public String deleteAcademy(HttpSession session) {
        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);

        if (loginUser == null || loginUser.getAcademyId() == null) {
            return "redirect:/";
        }

        academyService.deleteAcademy(loginUser.getAcademyId());
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/admin/classes")
    public String classes(HttpSession session, Model model) {
        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);

        if (loginUser == null || loginUser.getAcademyId() == null) {
            return "redirect:/";
        }

        Long academyId = loginUser.getAcademyId();
        List<AcademyClass> academyClasses = classauthService.findClassesByAcademyId(academyId);

        model.addAttribute("academyClasses", academyClasses);
        return "admin/classes";
    }

    @GetMapping("/admin/classes/{classId}")
    public String classDetail(
            @PathVariable Long classId,
            HttpSession session,
            Model model
    ) {
        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);

        if (loginUser == null || loginUser.getAcademyId() == null) {
            return "redirect:/";
        }

        Long academyId = loginUser.getAcademyId();
        ClassDetail classDetail = classauthService.findClassById(classId, academyId);

        if (classDetail == null) {
            return "redirect:/admin/classes";
        }

        model.addAttribute("classDetail", classDetail);
        return "admin/classDetail";
    }

    @PostMapping("/admin/updateClass")
    public String updateClass(
            @ModelAttribute AcademyClass classInfo,
            HttpSession session
    ) {
        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);

        if (loginUser == null || loginUser.getAcademyId() == null) {
            return "redirect:/";
        }

        classInfo.setAcademyId(loginUser.getAcademyId());
        classauthService.updateClass(classInfo);
        return "redirect:/admin/classes/" + classInfo.getClassId();
    }

    @PostMapping("/admin/deleteClass")
    public String deleteClass(
            @RequestParam Long classId,
            HttpSession session
    ) {
        User loginUser = (User) session.getAttribute(SessionConst.LOGIN_USER);

        if (loginUser == null || loginUser.getAcademyId() == null) {
            return "redirect:/";
        }

        classauthService.deleteClass(classId, loginUser.getAcademyId());
        return "redirect:/admin/classes";
    }
}
