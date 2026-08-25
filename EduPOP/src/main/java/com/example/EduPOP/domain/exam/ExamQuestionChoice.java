package com.example.EduPOP.domain.exam;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ExamQuestionChoice {

    private Long choiceId;
    private Long questionId;

    private Integer choiceNumber;
    private String choiceText;
    private Integer sortOrder;

}