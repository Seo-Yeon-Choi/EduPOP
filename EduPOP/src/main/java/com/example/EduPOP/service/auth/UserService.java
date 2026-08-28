package com.example.EduPOP.service.auth;

import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.domain.user.UserStatus;
import com.example.EduPOP.repository.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public boolean registerLocalUser(User user) {
        User existingUser = userMapper.findByLoginId(user.getLoginId());

        if (existingUser != null) {
            return false;
        }

        validatePassword(user.getPasswordHash());

        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));

        user.setStatus(UserStatus.PENDING);
        userMapper.saveUser(user);
        return true;
    }

    public User login(String loginId, String rawPassword) {
        User user = userMapper.findByLoginId(loginId);

        if (user == null ||
                !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            return null;
        }

        return user;
    }

    public User findByUserId(Long userId) {
        return userMapper.findByUserId(userId);
    }

    public List<User> getUsersAcademyId(Long academyId) {
        return userMapper.findUserByAcademyId(academyId);
    }

    @Transactional
    public void updateStatus(Long userId, UserStatus status) {
        userMapper.updateStatus(userId, status);
    }

    @Transactional
    public void updateUsersStatusBatch(List<Long> userIds, UserStatus status) {
        if (userIds != null && !userIds.isEmpty()) {
            userMapper.updateUsersStatusBatch(userIds, status);
        }
    }

    @Transactional
    public void withdrawUser(Long userId) {
        userMapper.updateStatus(userId, UserStatus.WITHDRAWN);
    }

    public void updateKakaoUserInfo(
            Long userId,
            Long academyId,
            String email,
            String phone,
            Integer schoolGrade
    ) {
        userMapper.updateKakaoUserInfo(userId, academyId, email, phone, schoolGrade);
    }

    @Transactional
    public User updateAccount(
            Long userId,
            User changes,
            String newPassword,
            String confirmPassword
    ) {
        User savedUser = userMapper.findByUserId(userId);

        if (savedUser == null || savedUser.getStatus() == UserStatus.WITHDRAWN) {
            throw new IllegalArgumentException("수정할 수 없는 계정입니다.");
        }

        String email = trimToNull(changes.getEmail());
        String phone = trimToNull(changes.getPhone());
        String schoolGrade = trimToNull(changes.getSchoolGrade());
        validateEmail(email);

        if (isSocialUser(savedUser)) {
            userMapper.updateSocialAccount(userId, email, phone, schoolGrade);
        } else {
            String name = trimToNull(changes.getName());

            if (name == null) {
                throw new IllegalArgumentException("이름을 입력해주세요.");
            }

            String password = validateNewPassword(newPassword, confirmPassword);
            String encodedPassword =
                    password == null
                            ? null
                            : passwordEncoder.encode(password);

            userMapper.updateLocalAccount(
                    userId,
                    encodedPassword,
                    name,
                    email,
                    phone,
                    schoolGrade
            );
        }

        return userMapper.findByUserId(userId);
    }

    public boolean isSocialUser(User user) {
        return hasText(user.getKakaoId())
                || hasText(user.getNaverId())
                || hasText(user.getGoogleId());
    }

    public String getLoginType(User user) {
        if (hasText(user.getKakaoId())) {
            return "카카오 로그인";
        }

        if (hasText(user.getNaverId())) {
            return "네이버 로그인";
        }

        if (hasText(user.getGoogleId())) {
            return "구글 로그인";
        }

        return "일반 로그인";
    }

    private void validatePassword(String password) {
        if (!hasText(password)) {
            throw new IllegalArgumentException("비밀번호를 입력해주세요.");
        }

        if (password.length() < 8 || password.length() > 64) {
            throw new IllegalArgumentException(
                    "비밀번호는 8자 이상 64자 이하로 입력해주세요."
            );
        }

        if (!password.matches(".*[A-Za-z].*")
                || !password.matches(".*\\d.*")) {
            throw new IllegalArgumentException(
                    "비밀번호에는 영문과 숫자를 모두 포함해주세요."
            );
        }
    }

    private String validateNewPassword(String newPassword, String confirmPassword) {
        String password = trimToNull(newPassword);
        String confirmation = trimToNull(confirmPassword);

        if (password == null && confirmation == null) {
            return null;
        }

        if (password == null || !password.equals(confirmation)) {
            throw new IllegalArgumentException("새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        validatePassword(password);

        return password;
    }

    private void validateEmail(String email) {
        if (email != null && !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("이메일 형식을 확인해주세요.");
        }
    }

    private String trimToNull(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}