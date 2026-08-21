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

        //로그인 시 회원 기본값
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
}
