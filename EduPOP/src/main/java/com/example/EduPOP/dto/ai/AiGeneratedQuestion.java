package com.example.EduPOP.dto.ai;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public class AiGeneratedQuestion {

    @JsonPropertyDescription(
            "유사 문제 생성의 원본 question_id"
    )
    public Long sourceQuestionId;

    @JsonPropertyDescription(
            "새롭게 생성된 문제 본문"
    )
    public String questionText;

    @JsonPropertyDescription(
            "객관식 선택지. 정확히 4개"
    )
    public List<String> choices;

    @JsonPropertyDescription(
            "정답 선택지 번호. 1부터 4 사이 정수"
    )
    public Integer correctAnswer;
}