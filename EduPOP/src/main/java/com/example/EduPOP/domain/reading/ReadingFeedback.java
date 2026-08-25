package com.example.EduPOP.domain.reading;

import lombok.Data;

import java.time.LocalDateTime;

@Data // Getter, Setter, toString 등 데이터 관리 메서드 자동 생성
public class ReadingFeedback {
    //교사 피드백 객체
    private Long feedbackId; // 피드백 번호

    private Long readingReportId; // 어떤 책에 감상문의 피드백인지 구별번호

    private Long teacherId; // 피드백을 작성한 교사 학번

    private String content; // 교사가 작성한 피드백 내용

    private LocalDateTime createdAt; // 피드백을 처음 작성한 시간

    private LocalDateTime updatedAt; // 피드백을 마지막으로 수정한 시간
}
