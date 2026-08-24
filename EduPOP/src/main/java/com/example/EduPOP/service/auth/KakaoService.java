package com.example.EduPOP.service.auth;

import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.domain.user.UserRole;
import com.example.EduPOP.domain.user.UserStatus;
import com.example.EduPOP.repository.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KakaoService {
    private final UserMapper userMapper;

    private final String KAKAO_CLIENT_ID = "f0d17d7cf78033e1ed7f979b9b09591b";
    private final String REDIRECT_URI = "http://localhost:8080/kakao/callback";

    @Transactional
    public User loginWithKakao(String code, UserRole role){
        //통신..
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            HttpHeaders tokenHeaders = new HttpHeaders();
            tokenHeaders.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

            // 편지 내용(바디) 작성: 카카오가 달라고 한 4가지 정보를 담습니다.
            MultiValueMap<String, String> tokenBody = new LinkedMultiValueMap<>();
            tokenBody.add("grant_type", "authorization_code");
            tokenBody.add("client_id", KAKAO_CLIENT_ID);
            tokenBody.add("redirect_uri", REDIRECT_URI);
            tokenBody.add("code", code);

            // 봉투와 내용을 하나의 우편물(HttpEntity)로 합쳐서 카카오 본사로 POST 방식으로 보냅니다.
            HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(tokenBody, tokenHeaders);
            ResponseEntity<String> tokenResponse = restTemplate.exchange(
                    "https://kauth.kakao.com/oauth/token",
                    HttpMethod.POST,
                    tokenRequest,
                    String.class
            );

            // 카카오가 답장으로 준 JSON 텍스트에서 'access_token' 글자만 쏙 뽑아냅니다.
            JsonNode tokenNode = objectMapper.readTree(tokenResponse.getBody());
            String accessToken = tokenNode.get("access_token").asText();


            // ========================================================
            // [두 번째 심부름] Access Token(입장권)을 보여주고 사용자 정보(이름) 받아오기
            // ========================================================

            // 새 편지 봉투 작성: 이번엔 방금 받은 입장권(토큰)을 봉투 겉면에 붙여서 내가 누군지 증명합니다.
            HttpHeaders userHeaders = new HttpHeaders();
            userHeaders.add("Authorization", "Bearer " + accessToken);
            userHeaders.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

            // 봉투만 보내면 되므로 내용은 비워둡니다.
            HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);
            ResponseEntity<String> userResponse = restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.POST,
                    userRequest,
                    String.class
            );

            // 카카오가 보내준 답장에서 회원 번호, 닉네임을 쏙쏙 뽑아냅니다.
            JsonNode userNode = objectMapper.readTree(userResponse.getBody());
            String kakaoId = userNode.get("id").asText();
            String name = userNode.get("properties").get("nickname").asText();

            //user가 DB에 있는지 확인
            User existingUser = userMapper.findByKakaoId(kakaoId);
            if (existingUser != null){
                return existingUser;
            }//DB에 없다면 새 계정 생성
            User newUser = new User();
            newUser.setKakaoId(kakaoId);
            newUser.setKakaoName(name);

            newUser.setKakaoEmail("이메일없음");
            newUser.setLogin_id("kakao_" + kakaoId);
            newUser.setName("kakao_"+name);
            newUser.setPassword_hash(UUID.randomUUID().toString());

            //처음 온 사람은 역할이 없어서 PENDING 상태
            newUser.setStatus(UserStatus.PENDING);
            newUser.setRole(role);



            //새로운 회원 저장
            userMapper.save(newUser);
            return newUser;

        } catch (Exception e) {
            System.out.println("카카오 통신 중 에러 발생: " + e.getMessage());
            throw new RuntimeException("카카오 로그인에 실패했습니다.");
        }

    }
}
