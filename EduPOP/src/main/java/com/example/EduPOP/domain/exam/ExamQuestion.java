package com.example.EduPOP.domain.exam;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExamQuestion {
    private Long questionId;
    private Long examId;
    private Integer questionNumber;
    private String questionType; // 문법: 관계사, 어휘: 다의어 등
    private Integer score;          // 문항 배점
    private String correctAnswer;   // 기준 정답 (1~5)
    private Integer sortOrder;
}