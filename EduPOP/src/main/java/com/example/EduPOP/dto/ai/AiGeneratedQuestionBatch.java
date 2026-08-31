package com.example.EduPOP.dto.ai;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public class AiGeneratedQuestionBatch {

    @JsonPropertyDescription(
            "각 원본 문제에 대응하여 생성된 유사 문제 목록"
    )
    public List<AiGeneratedQuestion> questions;
}