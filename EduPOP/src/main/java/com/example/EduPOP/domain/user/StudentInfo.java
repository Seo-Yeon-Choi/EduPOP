package com.example.EduPOP.domain.user;

import lombok.Data;

@Data
//관리자페이지 -> 학급관리에 띄울 학생 명단
public class StudentInfo {
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String status;
}