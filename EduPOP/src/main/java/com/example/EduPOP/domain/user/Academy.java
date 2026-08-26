package com.example.EduPOP.domain.user;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Academy {
    private Long academyId;
    private String name;
    private String address;
    private String phone;
    private String businessCer;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}