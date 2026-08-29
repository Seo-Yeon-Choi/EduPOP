package com.example.EduPOP.controller.exam.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

// OMR 스프레드시트 일괄 채점 저장용 DTO
@Data
@NoArgsConstructor
public class ExamBulkGradeRequest {
    private Long examId;
    private List<StudentGradePayload> studentGrades;

    @Data
    @NoArgsConstructor
    public static class StudentGradePayload {
        private Long studentId;
        private String studentName;
        private List<AnswerPayload> answers;
        private String teacherComment; // OMR에서 작성한 학부모 리포트용 코멘트
    }

    @Data
    @NoArgsConstructor
    public static class AnswerPayload {
        private Long questionId;
        private Integer questionNumber;
        private String submittedAnswer; // 학생이 입력한 1~5번
        private String correctAnswer;  // 정답
        private Integer score;         // 배점
        private String questionType;   // 문제유형
    }
}
