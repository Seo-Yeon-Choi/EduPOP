package com.example.EduPOP.repository.user;

import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.domain.user.UserRole;
import com.example.EduPOP.domain.user.UserStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    User findByUserId(@Param("userId") Long userId);

    User findByKakaoId(@Param("kakaoId") String kakaoId);

    User findByNaverId(@Param("naverId") String naverId);

    User findByGoogleId(String googleId);

    User findByLoginId(@Param("loginId") String loginId);

    void saveUser(User user);

    void updateAcademyAndStatus(
            @Param("userId") Long userId,
            @Param("academyId") Long academyId,
            @Param("role") UserRole role,
            @Param("status") UserStatus status
    );

    List<User> findUserByAcademyId(@Param("academyId") Long academyId);

    void updateStatus(
            @Param("userId") Long userId,
            @Param("status") UserStatus status
    );

    void updateUsersStatusBatch(
            @Param("userIds") List<Long> userIds,
            @Param("status") UserStatus status
    );

    void deleteOldWithdrawnUsers();

    void updateKakaoUserInfo(
            @Param("userId") Long userId,
            @Param("academyId") Long academyId,
            @Param("email") String email,
            @Param("phone") String phone,
            @Param("schoolGrade") String schoolGrade
    );

    void updateLocalAccount(
            @Param("userId") Long userId,
            @Param("passwordHash") String passwordHash,
            @Param("name") String name,
            @Param("email") String email,
            @Param("phone") String phone,
            @Param("schoolGrade") String schoolGrade
    );

    void updateSocialAccount(
            @Param("userId") Long userId,
            @Param("email") String email,
            @Param("phone") String phone,
            @Param("schoolGrade") String schoolGrade
    );
}