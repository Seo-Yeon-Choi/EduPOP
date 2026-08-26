package com.example.EduPOP.service.auth;

import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.domain.user.UserRole;
import com.example.EduPOP.domain.user.UserStatus;
import com.example.EduPOP.repository.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleService {

    private static final String TOKEN_URL =
            "https://oauth2.googleapis.com/token";

    private static final String USER_INFO_URL =
            "https://openidconnect.googleapis.com/v1/userinfo";

    private final UserMapper userMapper;

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    @Transactional
    public User loginWithGoogle(String code, UserRole role) throws Exception {

        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();

        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(
                MediaType.APPLICATION_FORM_URLENCODED
        );

        MultiValueMap<String, String> tokenParameters =
                new LinkedMultiValueMap<>();

        tokenParameters.add("code", code);
        tokenParameters.add("client_id", clientId);
        tokenParameters.add("client_secret", clientSecret);
        tokenParameters.add("redirect_uri", redirectUri);
        tokenParameters.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> tokenRequest =
                new HttpEntity<>(tokenParameters, tokenHeaders);

        ResponseEntity<String> tokenResponse =
                restTemplate.postForEntity(
                        TOKEN_URL,
                        tokenRequest,
                        String.class
                );

        if (!tokenResponse.getStatusCode().is2xxSuccessful()
                || tokenResponse.getBody() == null) {
            throw new IllegalStateException(
                    "Google access token 발급에 실패했습니다."
            );
        }

        JsonNode tokenJson =
                objectMapper.readTree(tokenResponse.getBody());

        String accessToken =
                getText(tokenJson, "access_token");

        if (accessToken == null) {
            throw new IllegalStateException(
                    "Google access token이 없습니다."
            );
        }

        HttpHeaders profileHeaders = new HttpHeaders();
        profileHeaders.setBearerAuth(accessToken);

        HttpEntity<Void> profileRequest =
                new HttpEntity<>(profileHeaders);

        ResponseEntity<String> profileResponse =
                restTemplate.exchange(
                        USER_INFO_URL,
                        HttpMethod.GET,
                        profileRequest,
                        String.class
                );

        if (!profileResponse.getStatusCode().is2xxSuccessful()
                || profileResponse.getBody() == null) {
            throw new IllegalStateException(
                    "Google 사용자 정보 조회에 실패했습니다."
            );
        }

        JsonNode profile =
                objectMapper.readTree(profileResponse.getBody());

        String googleId = getText(profile, "sub");
        String name = getText(profile, "name");
        String email = getText(profile, "email");

        if (googleId == null) {
            throw new IllegalStateException(
                    "Google 사용자 식별값이 없습니다."
            );
        }

        User existingUser =
                userMapper.findByGoogleId(googleId);

        if (existingUser != null) {
            return existingUser;
        }

        if (name == null || name.isBlank()) {
            name = "구글 사용자";
        }

        User newUser = new User();

        newUser.setGoogleId(googleId);
        newUser.setLoginId("google_" + googleId);
        newUser.setPasswordHash(UUID.randomUUID().toString());
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setRole(role);
        newUser.setStatus(UserStatus.PENDING);

        userMapper.saveUser(newUser);

        return newUser;
    }

    private String getText(JsonNode node, String fieldName) {

        JsonNode value = node.get(fieldName);

        if (value == null || value.isNull()) {
            return null;
        }

        String text = value.asText();

        return text.isBlank() ? null : text;
    }
}