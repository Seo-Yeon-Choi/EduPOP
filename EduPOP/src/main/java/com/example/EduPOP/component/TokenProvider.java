//package com.example.EduPOP.component;
//
//import com.example.EduPOP.domain.user.UserRole;
//import io.jsonwebtoken.*;
//import io.jsonwebtoken.security.Keys;
//import jakarta.annotation.PostConstruct;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import java.security.Key;
//import java.util.Date;
//
//// yml에게 값을 받아서 토큰 검증, 실패시 로그 남김 (=토큰을 다루는 역할)
//
//// @Component: 스프링에게 TokenProvider 설치해달라 지시
//@Component
//public class TokenProvider {
//    // application.yml 파일에서 secret(보안키) 가져옴
//    @Value("${jwt.secret}")
//    private String secretKey;
//    // application.yml 파일에서 expiration(만료시간) 가져옴
//    @Value("${jwt.expiration}")
//    private long expirationTime;
//    // 객체 생성
//    private Key key;
//
//    // 설치되자마자 가장 먼저 init()메서드 자동으로 1번만 실행 - 컴퓨터 속도 위해
//    @PostConstruct
//    public void init() {
//        //문자열 데이터의 secretKey를 컴퓨터가 계산할 수 있는 Byte로 형변환
//        //HS256 알고리즘에 사용할 수 있는 규격화된 Key 객체를 생성
//        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
//    }
//
//    //토큰 생성
//    public String createInvitationToken(Long academy_id, String role) {
//        // 현재 시간 확인합니다.
//        Date now = new Date();
//        // 현재 시간에 유통기한을 더해서 만료 시간 계산
//        Date validity = new Date(now.getTime() + expirationTime);
//
//        return Jwts.builder()
//                .setSubject("invitation")            // 제목: "이건 초대장이야!"
//                .claim("academy_id", academy_id)       // 내용물 1: "이 사람의 학원 번호는 이거야!"
//                .claim("role", role)                 // 내용물 2: "이 사람은 학생(또는 강사)이야!"
//                .setIssuedAt(now)                    // 적은 시간: "지금 만들었어."
//                .setExpiration(validity)             // 버릴 시간: "이 시간이 지나면 휴지통에 버려."
//                .signWith(key, SignatureAlgorithm.HS256) // 마지막으로 우리 학원만의 비밀 도장을 찍습니다.
//                .compact();                          // 하나의 암호문(토큰)으로 만듭니다.
//    }
//
//
//    // 초대링크를 누르고 왔을 때 claim에서 학원 번호 꺼냄
//    public Long getAcademyIdFromToken(String token) {
//        // Claims = io.jsonwebtoken에서 제공하는 전용 객체(클래스) , Key:Value 보관함 역할
//        Claims claims = getClaims(token);
//        // 'academyId(학원 번호)'를 찾아서 Long형태로 돌려줌
//        return claims.get("academy_id", Long.class);
//    }
//
//    // claim에서 역할(학생/강사) 꺼냄
//    public UserRole getRoleFromToken(String token) {
//        Claims claims = getClaims(token);
//        // 'role' 이라는 글자를 찾아서 UserRole 형태로 돌려줌
//        return UserRole.valueOf(claims.get("role", String.class));
//    }
//
//    // 가짜or만료인지 검사
//    public boolean validateToken(String token) {
//        try {
//            // Key로 토큰 검사
//            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
//            return true; // 토큰 인증 완료
//
//        } catch (Exception e) {
//            System.out.print("문제가 있는 토큰입니다: " + e.getMessage());
//            return false; // 인증 불가 (가짜이거나 만료된 토큰)
//        }
//    }
//
//    // 번호나 역할을 꺼낼 때 과정이 똑같아서 하나로 묶어둔 기능
//    private Claims getClaims(String token) {
//        return Jwts.parserBuilder()
//                .setSigningKey(key)
//                .build()
//                .parseClaimsJws(token)
//                .getBody(); // 이상이 없으면 내용물(Body = Claims)만 꺼내서 반환
//    }
//}