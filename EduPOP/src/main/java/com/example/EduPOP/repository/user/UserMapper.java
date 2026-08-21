package com.example.EduPOP.repository.user;

import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.domain.user.UserRole;
import com.example.EduPOP.domain.user.UserStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    User findById(@Param("user_id") Long user_id);
    User findByKakaoId(@Param("kakaoId") String kakaoId);
    User findByLoginId(@Param("login_id") String login_id);

    void save(User user);
    void updateAcademyAndStatus(@Param("user_id") Long user_id,
                                @Param("academy_id") Long academy_id,
                                @Param("role") UserRole role,
                                @Param("status") UserStatus status);
}
