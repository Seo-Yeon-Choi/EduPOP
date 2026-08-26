package com.example.EduPOP.domain.user;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {
    private Long userId;
    private Long academyId;
    private String academyName;
    private String loginId;
    private String passwordHash;
    private String name;
    private String email;
    private String phone;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime withdrawnAt;
    private String kakaoId;
    private String kakaoEmail;
    private String kakaoName;
    private String naverId;
    private String googleId;
    private String schoolGrade;
    private String className;
}
