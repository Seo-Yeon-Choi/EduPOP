package com.example.EduPOP.controller.auth;

import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.domain.user.UserRole;
import com.example.EduPOP.domain.user.UserStatus;
import com.example.EduPOP.service.auth.AcademyService;
import com.example.EduPOP.service.auth.SecurityLoginService;
import com.example.EduPOP.service.auth.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AcademyService academyService;

    @Mock
    private UserService userService;

    @Mock
    private SecurityLoginService securityLoginService;

    private AuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthController(
                academyService,
                userService,
                securityLoginService
        );
    }

    @Test
    void loginRouteStoresTheRequestedRole() {
        MockHttpSession session = new MockHttpSession();

        String view = controller.teacherLoginPage(session);

        assertThat(view).isEqualTo("/login");
        assertThat(session.getAttribute("requestedRole")).isEqualTo("TEACHER");
    }

    @Test
    void duplicateSignupReturnsToTheFormWithAnError() {
        User user = new User();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("requestedRole", "STUDENT");
        when(userService.registerLocalUser(user)).thenReturn(false);

        RedirectAttributesModelMap redirectAttributes =
                new RedirectAttributesModelMap();

        String view = controller.signUpProcess(
                user,
                session,
                redirectAttributes
        );

        assertThat(view).isEqualTo("redirect:/signUp");
        assertThat(user.getRole()).isEqualTo(UserRole.STUDENT);
        assertThat(redirectAttributes.getFlashAttributes())
                .containsEntry("error", "이미 사용 중인 아이디입니다.");
    }

    @Test
    void failedLoginDoesNotCreateASecurityContext() {
        when(userService.login("student", "wrong")).thenReturn(null);
        RedirectAttributesModelMap redirectAttributes =
                new RedirectAttributesModelMap();

        String view = controller.loginProcess(
                "student",
                "wrong",
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                new MockHttpSession(),
                redirectAttributes
        );

        assertThat(view).isEqualTo("redirect:/LocalLogin");
        assertThat(redirectAttributes.getFlashAttributes())
                .containsEntry("error", "아이디 또는 비밀번호가 올바르지 않습니다.");
        verifyNoInteractions(securityLoginService);
    }

    @Test
    void withdrawnUserCannotLogIn() {
        User withdrawn = user(7L, UserRole.STUDENT, UserStatus.WITHDRAWN);
        when(userService.login("student", "password")).thenReturn(withdrawn);

        String view = controller.loginProcess(
                "student",
                "password",
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                new MockHttpSession(),
                new RedirectAttributesModelMap()
        );

        assertThat(view).isEqualTo("redirect:/");
        verify(userService, never()).findByUserId(7L);
        verifyNoInteractions(securityLoginService);
    }

    @Test
    void activeStudentLoginUsesTheLatestDatabaseUser() {
        User loginResult = user(7L, UserRole.STUDENT, UserStatus.ACTIVE);
        User latestUser = user(7L, UserRole.STUDENT, UserStatus.ACTIVE);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(userService.login("student", "password")).thenReturn(loginResult);
        when(userService.findByUserId(7L)).thenReturn(latestUser);

        String view = controller.loginProcess(
                "student",
                "password",
                request,
                response,
                new MockHttpSession(),
                new RedirectAttributesModelMap()
        );

        assertThat(view).isEqualTo("/main/studentMain");
        verify(securityLoginService).login(latestUser, request, response);
    }

    @Test
    void pendingAdminLoginMovesToAdminWaiting() {
        User loginResult = user(30L, UserRole.ADMIN, UserStatus.PENDING);
        User latestUser = user(30L, UserRole.ADMIN, UserStatus.PENDING);

        when(userService.login("admin", "password")).thenReturn(loginResult);
        when(userService.findByUserId(30L)).thenReturn(latestUser);

        String view = controller.loginProcess(
                "admin",
                "password",
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                new MockHttpSession(),
                new RedirectAttributesModelMap()
        );

        assertThat(view).isEqualTo("redirect:/adminWaiting");
    }

    @Test
    void withdrawUsesOnlyTheLoggedInUserIdAndClearsTheSession() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(
                "loginUser",
                user(7L, UserRole.STUDENT, UserStatus.ACTIVE)
        );

        RedirectAttributesModelMap redirectAttributes =
                new RedirectAttributesModelMap();

        String view = controller.withdraw(session, redirectAttributes);

        assertThat(view).isEqualTo("redirect:/");
        assertThat(session.getAttribute("loginUser")).isNull();
        verify(userService).withdrawUser(7L);
        assertThat(redirectAttributes.getFlashAttributes())
                .containsEntry(
                        "message",
                        "회원 탈퇴가 완료되었습니다. 소중한 정보는 1년간 보관됩니다."
                );
    }

    private User user(Long userId, UserRole role, UserStatus status) {
        User user = new User();
        user.setUserId(userId);
        user.setRole(role);
        user.setStatus(status);
        return user;
    }
}
