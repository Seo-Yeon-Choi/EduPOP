package com.example.EduPOP.service.auth;

import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.domain.user.UserRole;
import com.example.EduPOP.domain.user.UserStatus;
import com.example.EduPOP.repository.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
public class NaverService {

    private final UserMapper userMapper;

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    @Value("${naver.redirect-uri}")
    private String redirectUri;

    @Transactional
    public User loginWithNaver(String code, String state, UserRole role) {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            HttpHeaders tokenHeaders = new HttpHeaders();
            tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> tokenBody = new LinkedMultiValueMap<>();
            tokenBody.add("grant_type", "authorization_code");
            tokenBody.add("client_id", clientId);
            tokenBody.add("client_secret", clientSecret);
            tokenBody.add("redirect_uri", redirectUri);
            tokenBody.add("code", code);
            tokenBody.add("state", state);

            HttpEntity<MultiValueMap<String, String>> tokenRequest =
                    new HttpEntity<>(tokenBody, tokenHeaders);

            ResponseEntity<String> tokenResponse = restTemplate.exchange(
                    "https://nid.naver.com/oauth2.0/token",
                    HttpMethod.POST,
                    tokenRequest,
                    String.class
            );

            JsonNode tokenNode = objectMapper.readTree(tokenResponse.getBody());
            JsonNode accessTokenNode = tokenNode.get("access_token");

            if (accessTokenNode == null) {
                throw new IllegalStateException("네이버 Access Token이 없습니다.");
            }

            String accessToken = accessTokenNode.asText();

            HttpHeaders profileHeaders = new HttpHeaders();
            profileHeaders.setBearerAuth(accessToken);

            HttpEntity<Void> profileRequest = new HttpEntity<>(profileHeaders);

            ResponseEntity<String> profileResponse = restTemplate.exchange(
                    "https://openapi.naver.com/v1/nid/me",
                    HttpMethod.GET,
                    profileRequest,
                    String.class
            );

            JsonNode profileRoot = objectMapper.readTree(profileResponse.getBody());
            JsonNode response = profileRoot.get("response");

            if (response == null || response.get("id") == null) {
                throw new IllegalStateException("네이버 회원 정보를 조회할 수 없습니다.");
            }

            String naverId = response.get("id").asText();
            String name = getText(response, "name");
            String nickname = getText(response, "nickname");
            String email = getText(response, "email");
            String phone = getText(response, "mobile");

            if (name == null || name.isBlank()) {
                name = nickname;
            }
            if (name == null || name.isBlank()) {
                name = "네이버 사용자";
            }

            User existingUser = userMapper.findByNaverId(naverId);
            if (existingUser != null) {
                return existingUser;
            }

            User newUser = new User();
            newUser.setNaverId(naverId);
            newUser.setLoginId("naver_" + naverId);
            newUser.setPasswordHash(UUID.randomUUID().toString());
            newUser.setName(name);
            newUser.setEmail(email);
            newUser.setPhone(phone);
            newUser.setRole(role);
            newUser.setStatus(UserStatus.PENDING);

            userMapper.saveUser(newUser);
            return newUser;
        } catch (Exception e) {
            throw new RuntimeException("네이버 로그인에 실패했습니다.", e);
        }
    }

    private String getText(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.asText();
    }
}
