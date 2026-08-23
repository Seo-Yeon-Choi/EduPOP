package com.example.EduPOP.domain.reading;

import lombok.Data;

import java.time.LocalDateTime;

@Data // Getter, Setter, toString 등 데이터 관리 메서드 자동 생성
public class ReadingReport {
    //독서 감상문 객체
    private Long readingReportId; // 독서감상문 번호

    private Long studentId; // 독서감상문을 작성한 학생 학번

    private Long bookId; // 어떤 책을 읽고 쓴 글인지 구별하기 위한 번호

    private String title; // 독서감상문 제목

    private String content; // 독서감상문 본문 저장

    private LocalDateTime submittedAt; // 독서감상문 제출 시간 저장

    private LocalDateTime updatedAt; // 독서감상문 마지막 수정 시간 저장
}
