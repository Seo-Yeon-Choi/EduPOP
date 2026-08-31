package com.example.EduPOP;

import com.example.EduPOP.dto.ai.AiGeneratedQuestion;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.StructuredResponse;
import com.openai.models.responses.StructuredResponseCreateParams;
import org.junit.jupiter.api.Test;

class AiStructuredQuestionTest {

    @Test
    void generateStructuredQuestion() {

        System.out.println(
                "===== AI Structured Output 테스트 시작 ====="
        );

        OpenAIClient client =
                OpenAIOkHttpClient.fromEnv();

        String prompt = """
                당신은 영어 학습용 복습 문제를 만드는 교사입니다.

                아래 원본 문제와 동일한 학습 개념을 평가하는
                새로운 객관식 문제 1개를 생성하세요.

                [원본 문제 ID]
                100

                [원본 문제]
                She ___ to school yesterday.

                [선택지]
                1. go
                2. goes
                3. went
                4. going

                [정답]
                3

                [조건]
                - sourceQuestionId는 반드시 100이어야 합니다.
                - 원본 문제를 그대로 복사하지 마세요.
                - 영어 과거시제를 평가하세요.
                - 난이도는 원본과 비슷하게 유지하세요.
                - 선택지는 정확히 4개 생성하세요.
                - 정답은 정확히 하나만 존재해야 합니다.
                - correctAnswer에는 정답 번호 1~4 중 하나를 넣으세요.
                """;

        StructuredResponseCreateParams<AiGeneratedQuestion> params =
                ResponseCreateParams.builder()
                        .model(ChatModel.GPT_5_MINI)
                        .input(prompt)
                        .text(AiGeneratedQuestion.class)
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
                        .orElseThrow();

        System.out.println();
        System.out.println(
                "원본 문제 ID = " +
                        generated.sourceQuestionId
        );

        System.out.println(
                "AI 문제 = " +
                        generated.questionText
        );

        System.out.println(
                "선택지 = " +
                        generated.choices
        );

        System.out.println(
                "정답 = " +
                        generated.correctAnswer
        );

        System.out.println(
                "================================"
        );
    }
}