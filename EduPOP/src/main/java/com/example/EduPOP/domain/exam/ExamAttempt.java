package com.example.EduPOP.domain.exam;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ExamAttempt {
    private Long attemptId;
    private Long examId;
    private Long studentId;
    private Integer attemptNo;
    private String entryMethod; // OMR
    private Double totalScore;
    private Double maxScore;
    private Integer correctCount;
    private Integer totalQuestionCount;
    private String primaryWeakTag; // 1위 취약 유형
    private LocalDateTime gradedAt;
}