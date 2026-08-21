package com.example.EduPOP.controller.auth.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private String status;   // SUCCESS 또는 ERROR
    private String message;  // 결과 메시지
    private T data;          // 실제 전달할 데이터 (링크 등)

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("SUCCESS", message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("ERROR", message, null);
    }
}