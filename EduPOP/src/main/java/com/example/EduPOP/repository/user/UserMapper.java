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

    User findByLoginId(@Param("loginId") String loginId);

    // user 저장
    void saveUser(User user);

    void updateAcademyAndStatus(
            @Param("userId") Long userId,
            @Param("academyId") Long academyId,
            @Param("role") UserRole role,
            @Param("status") UserStatus status
    );

    // 모든 회원 조회
//    List<User> findAllUsers();

    // 학원Id로 조회
    List<User> findUserByAcademyId(@Param("academyId") Long academyId);

    // 표에서 특정 회원 상태 변경
    void updateStatus(
            @Param("userId") Long userId,
            @Param("status") UserStatus status
    );

    void updateUsersStatusBatch(
            @Param("userIds") List<Long> userIds,
            @Param("status") UserStatus status
    );

    // withdrawn 회원 1년 자동삭제
    void deleteOldWithdrawnUsers();

    // kakao회원이 학원 선택 후 academyID 추가해줌
    void updateAcademyId(@Param("userId") Long userId,
                         @Param("academyId") Long academyId);
}

