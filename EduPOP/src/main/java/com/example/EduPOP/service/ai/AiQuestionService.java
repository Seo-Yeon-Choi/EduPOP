package com.example.EduPOP.service.ai;

import com.example.EduPOP.dto.ai.AiGeneratedQuestion;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.StructuredResponse;
import com.openai.models.responses.StructuredResponseCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AiQuestionService {

    private final OpenAIClient client;
    private final AiQuestionValidator validator;

    public AiQuestionService(
            AiQuestionValidator validator
    ) {

        this.validator = validator;

        this.client =
                OpenAIOkHttpClient.fromEnv();
    }

    public AiGeneratedQuestion generateSimilarQuestion(
            Long sourceQuestionId,
            String questionText,
            String choice1,
            String choice2,
            String choice3,
            String choice4,
            Integer correctAnswer,
            String largeCategory,
            String smallCategory
    ) {

        String prompt = buildPrompt(
                sourceQuestionId,
                questionText,
                choice1,
                choice2,
                choice3,
                choice4,
                correctAnswer,
                largeCategory,
                smallCategory
        );

        StructuredResponseCreateParams<AiGeneratedQuestion> params =
                ResponseCreateParams.builder()
                        .model(ChatModel.GPT_5_MINI)
                        .input(prompt)
                        .text(AiGeneratedQuestion.class)
                        .maxOutputTokens(700)
                        .build();

        StructuredResponse<AiGeneratedQuestion> response =
                client.responses().create(params);

        AiGeneratedQuestion generated =
                response.output().stream()
                        .flatMap(item ->
                                item.message().stream())
                        .flatMap(message ->
                                message.content().stream())
                        .flatMap(content ->
                                content.outputText().stream())
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "OpenAI가 문제를 반환하지 않았습니다."
                                )
                        );

        validator.validate(
                generated,
                sourceQuestionId,
                questionText
        );

        response.usage().ifPresent(usage -> {

            log.info(
                    "OpenAI 사용량 - input={}, output={}, total={}",
                    usage.inputTokens(),
                    usage.outputTokens(),
                    usage.totalTokens()
            );
        });

        return generated;
    }

    private String buildPrompt(
            Long sourceQuestionId,
            String questionText,
            String choice1,
            String choice2,
            String choice3,
            String choice4,
            Integer correctAnswer,
            String largeCategory,
            String smallCategory
    ) {

        return """
            당신은 학생의 오답 복습을 위한
            유사 문제를 만드는 전문 교사입니다.

            다음 문제를 분석하여
            같은 개념을 평가하는 새로운 객관식 문제를
            정확히 1개 생성하세요.

            [원본 문제 ID]
            %s

            [문제]
            %s

            [선택지]
            1. %s
            2. %s
            3. %s
            4. %s

            [정답]
            %s

            [대분류]
            %s

            [소분류]
            %s

            [생성 규칙]
            - 원본 문제를 그대로 복사하지 않습니다.
            - 원본과 동일한 대분류/소분류 개념을 평가합니다.
            - 난이도는 원본과 비슷하게 유지합니다.
            - 선택지는 정확히 4개입니다.
            - 정답은 하나만 존재해야 합니다.
            - correctAnswer는 1~4 사이 정수입니다.
            - sourceQuestionId는 반드시 %s입니다.
            """
                .formatted(
                        sourceQuestionId,
                        questionText,
                        choice1,
                        choice2,
                        choice3,
                        choice4,
                        correctAnswer,
                        largeCategory == null
                                ? "미분류"
                                : largeCategory,
                        smallCategory == null
                                ? "미분류"
                                : smallCategory,
                        sourceQuestionId
                );
    }
}