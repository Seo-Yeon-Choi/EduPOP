package com.example.EduPOP.service.auth;

import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.domain.user.UserRole;
import com.example.EduPOP.domain.user.UserStatus;
import com.example.EduPOP.repository.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    @Transactional
    public void updateUserRoleAndStatus(Long user_id, UserRole role){
        User user = userMapper.findById(user_id);
        //데이터가 없을 시 중단하는 에러 처리
        if (user == null){
            throw new
                    IllegalArgumentException("해당 회원을 찾을 수 없습니다.");
        }

        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);

        userMapper.save(user);
    }
}
