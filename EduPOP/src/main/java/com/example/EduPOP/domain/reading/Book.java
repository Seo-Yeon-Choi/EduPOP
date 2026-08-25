package com.example.EduPOP.domain.reading;

import lombok.Data;

import java.time.LocalDateTime;

@Data // Getter, Setter, toString 등 데이터 관리 메서드 자동 생성
public class Book {
    //책 객체

    private Long bookId; // books.book_id 어떤 책인지 번호로 번호로 구별

    private String title; // books.title 도서 제목

    private String author; // books.author 글쓴이 이름

    private String coverImageUrl; // books.cover_image_url 책 이미지 url경로

    private LocalDateTime createdAt; // books.created_at 책 등록한 날짜와 시간
}
