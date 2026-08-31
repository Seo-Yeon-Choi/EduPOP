package com.example.EduPOP;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import org.junit.jupiter.api.Test;

class AiQuestionGenerationTest {

    @Test
    void generateSimilarQuestion() {

        System.out.println("===== AI 유사 문제 생성 테스트 시작 =====");

        // Windows 환경변수 OPENAI_API_KEY 사용
        OpenAIClient client = OpenAIOkHttpClient.fromEnv();

        String prompt = """
                당신은 영어 학습용 시험 문제를 만드는 교사입니다.

                아래 원본 문제와 동일한 학습 개념을 평가하는
                새로운 객관식 문제 1개를 생성하세요.

                [원본 문제]
                She ___ to school yesterday.

                [선택지]
                1. go
                2. goes
                3. went
                4. going

                [정답]
                3. went

                [생성 조건]
                1. 원본 문제를 그대로 복사하지 마세요.
                2. 원본 문제와 동일하게 영어 과거시제를 평가하세요.
                3. 객관식 선택지는 반드시 4개로 만드세요.
                4. 정답은 반드시 하나만 존재해야 합니다.
                5. 정답 번호도 알려주세요.
                6. 간단한 해설도 작성하세요.

                아래 형식으로 출력하세요.

                문제:
                선택지:
                1.
                2.
                3.
                4.
                정답:
                해설:
                """;

        ResponseCreateParams params = ResponseCreateParams.builder()
                .model(ChatModel.GPT_5_MINI)
                .input(prompt)
                .build();

        Response response = client.responses().create(params);

        System.out.println();
        System.out.println("===== OpenAI 생성 결과 =====");

        response.output().stream()
                .flatMap(item -> item.message().stream())
                .flatMap(message -> message.content().stream())
                .flatMap(content -> content.outputText().stream())
                .forEach(outputText ->
                        System.out.println(outputText.text())
                );

        System.out.println("============================");
    }
}