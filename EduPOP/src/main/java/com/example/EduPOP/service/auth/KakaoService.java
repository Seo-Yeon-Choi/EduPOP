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
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
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
public class KakaoService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @Transactional
    public User loginWithKakao(String code, UserRole role) {

        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            HttpHeaders tokenHeaders = new HttpHeaders();
            tokenHeaders.add(
                    "Content-type",
                    "application/x-www-form-urlencoded;charset=utf-8"
            );

            MultiValueMap<String, String> tokenBody =
                    new LinkedMultiValueMap<>();

            tokenBody.add("grant_type", "authorization_code");
            tokenBody.add("client_id", clientId);
            tokenBody.add("redirect_uri", redirectUri);
            tokenBody.add("code", code);

            HttpEntity<MultiValueMap<String, String>> tokenRequest =
                    new HttpEntity<>(tokenBody, tokenHeaders);

            ResponseEntity<String> tokenResponse = restTemplate.exchange(
                    "https://kauth.kakao.com/oauth/token",
                    HttpMethod.POST,
                    tokenRequest,
                    String.class
            );

            JsonNode tokenNode =
                    objectMapper.readTree(tokenResponse.getBody());

            String accessToken =
                    tokenNode.get("access_token").asText();

            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.add(
                    "Authorization",
                    "Bearer " + accessToken
            );
            userHeaders.add(
                    "Content-type",
                    "application/x-www-form-urlencoded;charset=utf-8"
            );

            HttpEntity<Void> userRequest =
                    new HttpEntity<>(userHeaders);

            ResponseEntity<String> userResponse = restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.POST,
                    userRequest,
                    String.class
            );

            JsonNode userNode =
                    objectMapper.readTree(userResponse.getBody());

            String kakaoId =
                    userNode.get("id").asText();

            String name =
                    userNode.get("properties")
                            .get("nickname")
                            .asText();

            // user가 DB에 있는지 확인
            User existingUser =
                    userMapper.findByKakaoId(kakaoId);

            if (existingUser != null) {
                return existingUser;
            }

            // DB에 없다면 새 계정 생성
            User newUser = new User();

            newUser.setKakaoId(kakaoId);
            newUser.setKakaoName(name);
            newUser.setKakaoEmail("이메일없음");

            newUser.setLoginId("kakao_" + kakaoId);
            newUser.setName("kakao_" + name);
            newUser.setPasswordHash(
                    passwordEncoder.encode(
                            UUID.randomUUID().toString()
                    )
            );

            // 처음 온 사람은 PENDING 상태
            newUser.setStatus(UserStatus.PENDING);
            newUser.setRole(role);

            // 새로운 회원 저장
            userMapper.saveUser(newUser);

            return newUser;

        } catch (Exception e) {
            System.out.println(
                    "카카오 통신 중 에러 발생: " + e.getMessage()
            );

            throw new RuntimeException(
                    "카카오 로그인에 실패했습니다."
            );
        }
    }
}