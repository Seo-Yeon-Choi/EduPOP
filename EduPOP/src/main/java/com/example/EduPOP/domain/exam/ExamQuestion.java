package com.example.EduPOP.domain.exam;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ExamQuestion {
    private Long questionId;
    private Long examId;

    private Long sectionId;

    private Integer questionNumber;

    private String questionType;

    private String questionTypeTag;

    private BigDecimal score;

    private String correctAnswer;

    private String questionText;

    private String passage;

    private Integer sortOrder;

    private Long sourceQuestionId;


    /*
     * exam_questions 컬럼은 아니지만
     * 객관식 문제의 선지를 같이 관리
     */
    private List<ExamQuestionChoice> choices = new ArrayList<>();

}