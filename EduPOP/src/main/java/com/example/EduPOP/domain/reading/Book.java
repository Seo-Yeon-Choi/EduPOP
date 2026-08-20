package com.example.EduPOP.domain.reading;

import lombok.Data;

import java.time.LocalDateTime;

@Data // Getter, Setter, toString 등 데이터 관리 메서드 자동 생성
public class Book {

    private Long bookId; // books.book_id 도서 고유번호 저장

    private String title; // books.title 도서 제목 저장

    private String author; // books.author 저자 이름 저장

    private String coverImageUrl; // books.cover_image_url 표지 이미지 주소 저장

    private LocalDateTime createdAt; // books.created_at 도서 등록 시각 저장
}
