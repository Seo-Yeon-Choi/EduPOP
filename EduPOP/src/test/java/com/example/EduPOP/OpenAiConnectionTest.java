package com.example.EduPOP;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import org.junit.jupiter.api.Test;

class OpenAiConnectionTest {

    @Test
    void openAiConnectionTest() {

        System.out.println("===== OpenAI API 연결 테스트 시작 =====");

        // OPENAI_API_KEY 환경변수를 자동으로 읽음
        OpenAIClient client = OpenAIOkHttpClient.fromEnv();

        ResponseCreateParams params = ResponseCreateParams.builder()
                .model(ChatModel.GPT_5_MINI)
                .input(
                        """
                        OpenAI API 연결 테스트입니다.
                        반드시 아래 문장만 출력하세요.

                        EduPOP OpenAI API 연결 성공
                        """
                )
                .build();

        Response response = client.responses().create(params);

        System.out.println("===== OpenAI 응답 =====");

        response.output().stream()
                .flatMap(item -> item.message().stream())
                .flatMap(message -> message.content().stream())
                .flatMap(content -> content.outputText().stream())
                .forEach(outputText ->
                        System.out.println(outputText.text())
                );

        System.out.println("=======================");
    }
}