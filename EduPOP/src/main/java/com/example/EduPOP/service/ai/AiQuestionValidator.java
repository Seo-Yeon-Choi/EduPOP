package com.example.EduPOP.service.ai;

import com.example.EduPOP.dto.ai.AiGeneratedQuestion;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;

@Component
public class AiQuestionValidator {

    public void validate(
            AiGeneratedQuestion question,
            Long expectedSourceQuestionId,
            String originalQuestionText
    ) {

        if (question == null) {
            throw new IllegalArgumentException(
                    "AI 문제 생성 결과가 없습니다."
            );
        }

        // 원본 문제 ID 확인
        if (question.sourceQuestionId == null) {
            throw new IllegalArgumentException(
                    "AI 문제의 원본 문제 ID가 없습니다."
            );
        }

        if (!expectedSourceQuestionId.equals(
                question.sourceQuestionId
        )) {
            throw new IllegalArgumentException(
                    "AI가 잘못된 원본 문제 ID를 반환했습니다."
            );
        }

        // 문제 본문 확인
        if (question.questionText == null ||
                question.questionText.isBlank()) {

            throw new IllegalArgumentException(
                    "AI 문제 본문이 없습니다."
            );
        }

        // 원본과 완전히 동일한 문제 방지
        if (originalQuestionText != null &&
                originalQuestionText.trim()
                        .equalsIgnoreCase(
                                question.questionText.trim()
                        )) {

            throw new IllegalArgumentException(
                    "AI가 원본 문제를 그대로 복사했습니다."
            );
        }

        // 선택지 확인
        if (question.choices == null ||
                question.choices.size() != 4) {

            throw new IllegalArgumentException(
                    "AI 문제의 선택지는 반드시 4개여야 합니다."
            );
        }

        for (String choice : question.choices) {

            if (choice == null ||
                    choice.isBlank()) {

                throw new IllegalArgumentException(
                        "AI 문제에 빈 선택지가 존재합니다."
                );
            }
        }

        // 선택지 중복 검사
        List<String> normalizedChoices =
                question.choices.stream()
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .toList();

        if (new HashSet<>(normalizedChoices).size() != 4) {

            throw new IllegalArgumentException(
                    "AI 문제에 중복된 선택지가 존재합니다."
            );
        }

        // 정답 번호 확인
        if (question.correctAnswer == null ||
                question.correctAnswer < 1 ||
                question.correctAnswer > 4) {

            throw new IllegalArgumentException(
                    "AI 문제의 정답 번호가 잘못되었습니다."
            );
        }
    }
}