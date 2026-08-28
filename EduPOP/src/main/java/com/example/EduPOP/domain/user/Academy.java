package com.example.EduPOP.domain.user;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Academy {
    private Long academyId;
    private String name;
    private String address;
    private String phone;
    private String businessCer;

    private String businessNumber;
    private String representativeName;
    private LocalDate businessStartDate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}