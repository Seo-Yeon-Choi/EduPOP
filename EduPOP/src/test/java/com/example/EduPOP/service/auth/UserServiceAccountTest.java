package com.example.EduPOP.service.auth;

import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.domain.user.UserStatus;
import com.example.EduPOP.repository.user.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceAccountTest {

    @Mock
    private UserMapper userMapper;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userMapper);
    }

    @Test
    void localUserCanUpdateProfileAndPassword() {
        User savedUser = localUser();
        User changes = changes("새 이름");
        User updatedUser = localUser();
        updatedUser.setName("새 이름");

        when(userMapper.findByUserId(1L)).thenReturn(savedUser, updatedUser);

        userService.updateAccount(1L, changes, "new-password", "new-password");

        verify(userMapper).updateLocalAccount(
                1L,
                "new-password",
                "새 이름",
                "new@example.com",
                "010-1111-2222",
                "3"
        );
        verify(userMapper, never()).updateSocialAccount(anyLong(), any(), any(), any());
    }

    @Test
    void socialUserCanOnlyUpdateEmailPhoneAndGrade() {
        User savedUser = localUser();
        savedUser.setKakaoId("kakao-123");
        User changes = changes("조작한 이름");
        User updatedUser = localUser();
        updatedUser.setKakaoId("kakao-123");

        when(userMapper.findByUserId(1L)).thenReturn(savedUser, updatedUser);

        userService.updateAccount(1L, changes, "ignored-password", "ignored-password");

        assertTrue(userService.isSocialUser(savedUser));
        verify(userMapper).updateSocialAccount(
                1L,
                "new@example.com",
                "010-1111-2222",
                "3"
        );
        verify(userMapper, never()).updateLocalAccount(
                anyLong(), any(), any(), any(), any(), any()
        );
    }

    @Test
    void localUserPasswordConfirmationMustMatch() {
        User savedUser = localUser();
        User changes = changes("새 이름");

        when(userMapper.findByUserId(1L)).thenReturn(savedUser);

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateAccount(1L, changes, "new-password", "different")
        );

        verify(userMapper, never()).updateLocalAccount(
                anyLong(), any(), any(), any(), any(), any()
        );
        verify(userMapper, never()).updateSocialAccount(anyLong(), any(), any(), any());
    }

    private User localUser() {
        User user = new User();
        user.setUserId(1L);
        user.setLoginId("student01");
        user.setAcademyId(10L);
        user.setName("기존 이름");
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }

    private User changes(String name) {
        User user = new User();
        user.setName(name);
        user.setEmail("new@example.com");
        user.setPhone("010-1111-2222");
        user.setSchoolGrade("3");
        user.setLoginId("attempted-change");
        user.setAcademyId(999L);
        return user;
    }
}
