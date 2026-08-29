package com.example.EduPOP.controller.exam.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class ExamCreateOrCopyRequest {
    private Long academyId;
    private Long teacherId;            // 로그인한 교사 ID를 서버에서 넣는다.

    private Long classId;              // 배정 대상 반 PK
    private Long templateExamId;       // 복제할 서식 ID (선택)
    private String title;              // 시험명
    private String examType;           // WORD, MONTHLY, REVIEW, OTHER
    private Integer examRound;         // 회차 (예: 1회차)
    private String examDate;           // 시행일 (예: 2026-08-25)

    // 직접 입력한 문항 목록 (커스텀 문항 등록 시)
    private List<CustomQuestionDto> customQuestions;

    // MyBatis useGeneratedKeys로 채워질 신규 시험 PK
    private Long generatedExamId;

    @Data
    @NoArgsConstructor
    public static class CustomQuestionDto {
        private Integer questionNumber;
        private String questionType;
        private String questionTypeTag;
        private Integer score;
        private String correctAnswer;
    }
}
