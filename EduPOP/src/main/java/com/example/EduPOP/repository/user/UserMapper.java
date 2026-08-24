package com.example.EduPOP.repository.user;

import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.domain.user.UserRole;
import com.example.EduPOP.domain.user.UserStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
//    User findById(@Param("user_id") Long user_id);
    User findByKakaoId(@Param("kakaoId") String kakaoId);
    User findByLoginId(@Param("login_id") String login_id);

    //user 저장
    void save(User user);
    //
    void updateAcademyAndStatus(@Param("user_id") Long user_id,
                                @Param("academy_id") Long academy_id,
                                @Param("role") UserRole role,
                                @Param("status") UserStatus status);


    // 모든 회원 조회
    List<User> findAllUsers();

    // 표에서 특정 회원 상태 변경
    void updateStatus(@Param("user_id") Long user_id, @Param("status") UserStatus status);

    void updateUsersStatusBatch(@Param("userIds") List<Long> userIds, @Param("status") UserStatus status);

    // withdrawn 회원 1년 자동삭제
    void deleteOldWithdrawnUsers();
}



