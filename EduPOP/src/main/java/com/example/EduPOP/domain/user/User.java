package com.example.EduPOP.domain.user;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class User {
    private Long user_id;
    private Long academy_id;
    private String login_id;
    private String password_hash;
    private String name;
    private String email;
    private String phone;

    private UserRole role;
    private UserStatus status;

    private LocalDateTime created_at;
    private LocalDateTime updated_at;
    private LocalDateTime withdrawn_at;

    private String kakaoId;
    private String kakaoEmail;
    private String kakaoName;

    private String school_grade;
    private String class_name;
}
