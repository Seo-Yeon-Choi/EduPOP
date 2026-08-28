package com.example.EduPOP.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth

                        // 로그인하지 않아도 접근 가능
                        .requestMatchers(
                                "/",
                                "/LocalLogin",
                                "/signUp",
                                "/login",
                                "/login/route/**",

                                "/kakao/**",
                                "/naver/**",
                                "/google/**",

                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico",
                                "/error"
                        ).permitAll()

                        // 학생 전용
                        .requestMatchers(
                                "/student/**",
                                "/main/studentMain"
                        ).hasRole("STUDENT")

                        // 교사 전용
                        .requestMatchers(
                                "/teacher/**",
                                "/exam/**",
                                //"/classroom/**",
                                "/main/teacherMain"
                        ).hasRole("TEACHER")

                        // 관리자 전용
                        .requestMatchers(
                                "/admin/**",
                                "/main/adminMain",
                                "/classroom/**",
                                "/adminWaiting"
                        ).hasRole("ADMIN")

                        // 로그인은 필요하지만 Role에 관계없는 페이지
                        .requestMatchers(
                                "/account/**",
                                "/selectAcademy",
                                "/blankPage",
                                "/user/withdraw"
                        ).authenticated()

                        // 나머지는 기본적으로 로그인 필요
                        .anyRequest().authenticated()
                )

                // 로그인하지 않은 사용자가 보호 URL 접근
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(
                                new LoginUrlAuthenticationEntryPoint(
                                        "/LocalLogin"
                                )
                        )
                )

                // Spring Security 로그아웃
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }
}