package com.example.EduPOP.domain.user;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class Academy {
    private Long academy_id;
    private String name;
    private String address;
    private String phone;
    private String business_cer;

    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}
