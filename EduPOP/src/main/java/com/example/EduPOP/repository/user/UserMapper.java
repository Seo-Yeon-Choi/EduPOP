package com.example.EduPOP.repository.user;

import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.domain.user.UserRole;
import com.example.EduPOP.domain.user.UserStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

    // 상태가 PENDING인 유저들만 골라오는 직원
    List<User> findPendingUsers();

    // 특정 유저의 상태를 ACTIVE로 바꿔주는 직원
    void updateUserStatus(@Param("user_id") Long user_id, @Param("status") UserStatus status);

    // 모든 회원 조회
    List<User> findAllUsers();

    // 특정 회원의 상태를 원하는 상태로 변경
    void updateStatus(@Param("user_id") Long user_id, @Param("status") UserStatus status);

    void updateUsersStatusBatch(@Param("userIds") List<Long> userIds, @Param("status") UserStatus status);
}



