package com.example.EduPOP.controller.auth;

import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.domain.user.UserRole;
import com.example.EduPOP.service.auth.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private UserService userService;

    private AccountController controller;

    @BeforeEach
    void setUp() {
        controller = new AccountController(userService);
    }

    @Test
    void detailRedirectsWhenNoUserIsLoggedIn() {
        String view = controller.accountDetail(
                new MockHttpSession(),
                new ExtendedModelMap()
        );

        assertThat(view).isEqualTo("redirect:/LocalLogin");
        verifyNoInteractions(userService);
    }

    @Test
    void detailLoadsSocialLoginInformationAndRoleHome() {
        User sessionUser = user(7L, UserRole.STUDENT);
        User accountUser = user(7L, UserRole.STUDENT);
        MockHttpSession session = session(sessionUser);

        when(userService.findByUserId(7L)).thenReturn(accountUser);
        when(userService.isSocialUser(accountUser)).thenReturn(true);
        when(userService.getLoginType(accountUser)).thenReturn("KAKAO");

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.accountDetail(session, model);

        assertThat(view).isEqualTo("account/detail");
        assertThat(model.get("accountUser")).isSameAs(accountUser);
        assertThat(model.get("socialUser")).isEqualTo(true);
        assertThat(model.get("loginType")).isEqualTo("KAKAO");
        assertThat(model.get("homeUrl")).isEqualTo("/main/studentMain");
    }

    @Test
    void editLoadsTheLatestAccountFromTheDatabase() {
        User sessionUser = user(20L, UserRole.TEACHER);
        User accountUser = user(20L, UserRole.TEACHER);
        MockHttpSession session = session(sessionUser);

        when(userService.findByUserId(20L)).thenReturn(accountUser);
        when(userService.isSocialUser(accountUser)).thenReturn(false);
        when(userService.getLoginType(accountUser)).thenReturn("LOCAL");

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.accountEdit(session, model);

        assertThat(view).isEqualTo("account/edit");
        assertThat(model.get("accountUser")).isSameAs(accountUser);
        assertThat(model.get("homeUrl")).isEqualTo("/main/teacherMain");
        verify(userService).findByUserId(20L);
    }

    @Test
    void updateRefreshesTheSessionWithTheSavedUser() {
        User sessionUser = user(7L, UserRole.STUDENT);
        User changes = new User();
        User updatedUser = user(7L, UserRole.STUDENT);
        MockHttpSession session = session(sessionUser);

        when(userService.updateAccount(
                7L,
                changes,
                "new-password",
                "new-password"
        )).thenReturn(updatedUser);

        RedirectAttributesModelMap redirectAttributes =
                new RedirectAttributesModelMap();

        String view = controller.updateAccount(
                changes,
                "new-password",
                "new-password",
                session,
                redirectAttributes
        );

        assertThat(view).isEqualTo("redirect:/account");
        assertThat(session.getAttribute("loginUser")).isSameAs(updatedUser);
        assertThat(redirectAttributes.getFlashAttributes())
                .containsEntry("message", "계정 정보가 수정되었습니다.");
    }

    @Test
    void updateKeepsValidationMessageForTheEditPage() {
        User changes = new User();
        MockHttpSession session = session(user(7L, UserRole.STUDENT));

        when(userService.updateAccount(7L, changes, "a", "b"))
                .thenThrow(new IllegalArgumentException("비밀번호 확인이 일치하지 않습니다."));

        RedirectAttributesModelMap redirectAttributes =
                new RedirectAttributesModelMap();

        String view = controller.updateAccount(
                changes,
                "a",
                "b",
                session,
                redirectAttributes
        );

        assertThat(view).isEqualTo("redirect:/account/edit");
        assertThat(redirectAttributes.getFlashAttributes())
                .containsEntry("error", "비밀번호 확인이 일치하지 않습니다.");
    }

    private User user(Long userId, UserRole role) {
        User user = new User();
        user.setUserId(userId);
        user.setRole(role);
        return user;
    }

    private MockHttpSession session(User user) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginUser", user);
        return session;
    }
}
