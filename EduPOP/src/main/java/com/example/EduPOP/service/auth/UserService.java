package com.example.EduPOP.service.auth;

import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.domain.user.UserRole;
import com.example.EduPOP.domain.user.UserStatus;
import com.example.EduPOP.repository.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    @Transactional
    public boolean registerLocalUser(User user) {

        // 중복 확인
        User existingUser =
                userMapper.findByLoginId(user.getLoginId());

        if (existingUser != null) {
            return false;
        }

        // 로그인 시 회원상태 기본값 PENDING
        user.setStatus(UserStatus.PENDING);

        userMapper.saveUser(user);

        return true;
    }

    public User login(String loginId, String passwordHash) {

        User user = userMapper.findByLoginId(loginId);

        if (user == null ||
                !passwordHash.equals(user.getPasswordHash())) {
            return null;
        }

        return user;
    }

    public User findByUserId(Long userId) {
        return userMapper.findByUserId(userId);
    }

    // 회원 조회
//    // ADMIN을 제외한 모든 회원 목록 가져오기
//    public List<User> getAllUsersExceptAdmin() {
//
//        List<User> allUsers =
//                userMapper.findAllUsers();
//
//        // ADMIN이 아닌 유저들만 필터링
//        return allUsers.stream()
//                .filter(user -> user.getRole() != UserRole.ADMIN)
//                .toList();
//    }

    //학원Id로 조회
    public List<User> getUsersAcademyId(Long academyId){
        return userMapper.findUserByAcademyId(academyId);
    }

    // 관리자가 표에서 특정 회원의 상태를 직접 변경
    @Transactional
    public void updateStatus(
            Long userId,
            UserStatus status
    ) {
        userMapper.updateStatus(userId, status);
    }

    // 여러 명 일괄 상태 변경
    @Transactional
    public void updateUsersStatusBatch(
            List<Long> userIds,
            UserStatus status
    ) {
        if (userIds != null && !userIds.isEmpty()) {
            userMapper.updateUsersStatusBatch(
                    userIds,
                    status
            );
        }
    }

    // 회원 탈퇴 (휴지통으로 이동)
    @Transactional
    public void withdrawUser(Long userId) {
        // mapper가 withdrawnAt 시간을 찍어줌
        userMapper.updateStatus(
                userId,
                UserStatus.WITHDRAWN
        );
    }

    // kakao회원이 학원 선택 후 academyID 추가해줌
    public void updateKakaoUserInfo(Long userId,
                                    Long academyId,
                                    String email,
                                    String phone,
                                    Integer schoolGrade){
        userMapper.updateKakaoUserInfo(
                userId,
                academyId,
                email,
                phone,
                schoolGrade);
    }

}

