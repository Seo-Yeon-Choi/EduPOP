package com.example.EduPOP.controller.exam.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// OMR의 학부모 리포트용 코멘트 일괄 저장 요청 DTO
@Data
@NoArgsConstructor
public class ExamCommentSaveRequest {

    private Long examId; // 코멘트가 작성된 시험 번호
    private List<StudentCommentPayload> comments; // 학생별 코멘트 목록

    @Data
    @NoArgsConstructor
    public static class StudentCommentPayload {
        private Long studentId; // 코멘트 대상 학생 번호
        private String comment; // 학부모 화면에 그대로 보여줄 글
    }
}
