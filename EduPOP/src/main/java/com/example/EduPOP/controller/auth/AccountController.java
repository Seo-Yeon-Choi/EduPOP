package com.example.EduPOP.controller.auth;

import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.domain.user.UserRole;
import com.example.EduPOP.service.auth.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final UserService userService;

    @GetMapping("/account")
    public String accountDetail(HttpSession session, Model model) {
        User loginUser = getLoginUser(session);

        if (loginUser == null) {
            return "redirect:/LocalLogin";
        }

        User accountUser = userService.findByUserId(loginUser.getUserId());
        addAccountModel(model, accountUser);
        return "account/detail";
    }

    @GetMapping("/account/edit")
    public String accountEdit(HttpSession session, Model model) {
        User loginUser = getLoginUser(session);

        if (loginUser == null) {
            return "redirect:/LocalLogin";
        }

        User accountUser = userService.findByUserId(loginUser.getUserId());
        addAccountModel(model, accountUser);
        return "account/edit";
    }

    @PostMapping("/account/edit")
    public String updateAccount(
            @ModelAttribute User changes,
            @RequestParam(required = false, defaultValue = "") String newPassword,
            @RequestParam(required = false, defaultValue = "") String confirmPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        User loginUser = getLoginUser(session);

        if (loginUser == null) {
            return "redirect:/LocalLogin";
        }

        try {
            User updatedUser = userService.updateAccount(
                    loginUser.getUserId(),
                    changes,
                    newPassword,
                    confirmPassword
            );

            session.setAttribute("loginUser", updatedUser);
            redirectAttributes.addFlashAttribute("message", "계정 정보가 수정되었습니다.");
            return "redirect:/account";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/account/edit";
        }
    }

    private User getLoginUser(HttpSession session) {
        return (User) session.getAttribute("loginUser");
    }

    private void addAccountModel(Model model, User accountUser) {
        model.addAttribute("accountUser", accountUser);
        model.addAttribute("socialUser", userService.isSocialUser(accountUser));
        model.addAttribute("loginType", userService.getLoginType(accountUser));
        model.addAttribute("homeUrl", getHomeUrl(accountUser.getRole()));
    }

    private String getHomeUrl(UserRole role) {
        if (role == UserRole.STUDENT) {
            return "/main/studentMain";
        }

        if (role == UserRole.TEACHER) {
            return "/main/teacherMain";
        }

        if (role == UserRole.ADMIN) {
            return "/main/adminMain";
        }

        return "/";
    }
}
