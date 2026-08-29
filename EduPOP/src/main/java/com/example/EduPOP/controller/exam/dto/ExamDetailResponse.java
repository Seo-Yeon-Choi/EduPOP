package com.example.EduPOP.controller.exam.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

/**
 * [역할: OMR 고속 채점 화면 렌더링용 종합 DTO]
 * 시험 기본 정보 + 기준 정답 목록 + 배정된 반의 실제 수강생 명단을 함께 담습니다.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExamDetailResponse {
    private Long examId;
    private String title;
    private Integer examRound;
    private Long classId;
    private String className;
    private String examDate;


    // 시험 문항 및 기준 정답 리스트
    private List<QuestionMetaInfo> questions;

    // 해당 반에 소속된 실제 학생 목록 (OMR 채점 테이블 행 구성용)
    private List<StudentRosterDto> students;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QuestionMetaInfo {
        private Long questionId;
        private Integer questionNumber;
        private String questionType; // VOCAB, GRAMMAR, READING 등
        private Double score;
        private String correctAnswer;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StudentRosterDto {
        private Long studentId;
        private String studentName;
        private String schoolGrade;
        private String parentPhone;

        private Double savedTotalScore; // 기존 저장된 총점
        private String savedAnswers;    // 기존 저장된 답안 문자열 (공백 구분)
        private Boolean isGraded;       // 채점 완료 여부

        private String teacherComment;
    }
}