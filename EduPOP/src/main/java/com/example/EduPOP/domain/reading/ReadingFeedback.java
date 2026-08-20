package com.example.EduPOP.domain.reading;

import lombok.Data;

import java.time.LocalDateTime;

@Data // Getter, Setter, toString 등 데이터 관리 메서드 자동 생성
public class ReadingFeedback {
    private Long feedbackId; // 첨삭 고유번호 저장

    private Long readingReportId; // 어떤 독서감상문의 첨삭인지 번호 저장

    private Long teacherId; // 첨삭을 작성한 교사 번호 저장

    private String content; // 교사가 작성한 첨삭 내용 저장

    private LocalDateTime createdAt; // 첨삭을 처음 작성한 시각 저장

    private LocalDateTime updatedAt; // 첨삭을 마지막으로 수정한 시각 저장
}
