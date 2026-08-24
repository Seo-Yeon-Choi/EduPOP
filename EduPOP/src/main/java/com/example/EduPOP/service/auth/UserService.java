package com.example.EduPOP.service.auth;

import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.domain.user.UserRole;
import com.example.EduPOP.domain.user.UserStatus;
import com.example.EduPOP.repository.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    @Transactional

    // 중복 확인
    public boolean registerLocalUser(User user){
        User existingUser = userMapper.findByLoginId(user.getLogin_id());
        if (existingUser != null){
           return false;
        }
        //로그인 시 회원상태 기본값 PENDING
        user.setStatus(UserStatus.PENDING);
        userMapper.save(user);
        return true;
    }


    public User login(String login_id, String password_hash){
        User user = userMapper.findByLoginId(login_id);

        if (user==null || !password_hash.equals(user.getPassword_hash())){
        return  null;
        }

        return user;
    }

    public User findById(Long user_id){
       return userMapper.findById(user_id);
    }


//---------------------------------------------------------------------------------------------------------------------
    // 회원 조회
    // ADMIN을 제외한 모든 회원 목록 가져오기
    public List<User> getAllUsersExceptAdmin() {
        List<User> allUsers = userMapper.findAllUsers();

        // 스트림을 이용해 role이 ADMIN이 아닌 유저들만 필터링
        return allUsers.stream()
                .filter(user -> user.getRole() != UserRole.ADMIN) // ADMIN이 아닌 것만
                .toList();
    }

    // 관리자가 표에서 특정 회원의 상태를 직접 변경
    @Transactional
    public void updateStatus(Long user_id, UserStatus status) {
        userMapper.updateStatus(user_id, status);
    }

    // 여러 명 일괄 상태 변경
    @Transactional
    public void updateUsersStatusBatch(List<Long> userIds, UserStatus status) {
        if (userIds != null && !userIds.isEmpty()) {
            userMapper.updateUsersStatusBatch(userIds, status);
        }
    }

//---------------------------------------------------------------------------------------------------------------------
    // 회원 탈퇴 (휴지통으로 이동)
    @Transactional
    public void withdrawUser(Long user_id) {
        // 맵퍼가 withdrawn_at 시간 찍어줌
        userMapper.updateStatus(user_id, UserStatus.WITHDRAWN);
    }
}
