package com.example.EduPOP.dto.ai;

import java.util.List;

public class AiSourceQuestion {

    private Long questionId;
    private String questionText;
    private List<String> choices;
    private Integer correctAnswer;
    private String questionTypeTag;

    public AiSourceQuestion() {
    }

    public AiSourceQuestion(
            Long questionId,
            String questionText,
            List<String> choices,
            Integer correctAnswer,
            String questionTypeTag
    ) {
        this.questionId = questionId;
        this.questionText = questionText;
        this.choices = choices;
        this.correctAnswer = correctAnswer;
        this.questionTypeTag = questionTypeTag;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public List<String> getChoices() {
        return choices;
    }

    public Integer getCorrectAnswer() {
        return correctAnswer;
    }

    public String getQuestionTypeTag() {
        return questionTypeTag;
    }
}