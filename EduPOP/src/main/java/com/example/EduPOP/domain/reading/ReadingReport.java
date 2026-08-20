package com.example.EduPOP.domain.reading;

import lombok.Data;

import java.time.LocalDateTime;

@Data // Getter, Setter, toString 등 데이터 관리 메서드 자동 생성
public class ReadingReport {

    private Long readingReportId; // 독서감상문 고유번호 저장

    private Long studentId; // 독서감상문을 작성한 학생 번호 저장

    private Long bookId; // 독서감상문과 연결된 도서 번호 저장

    private String title; // 독서감상문 제목 저장

    private String content; // 독서감상문 본문 저장

    private LocalDateTime submittedAt; // 독서감상문 제출 시각 저장

    private LocalDateTime updatedAt; // 독서감상문 마지막 수정 시각 저장
}
