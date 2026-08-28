package com.example.EduPOP.config;

import com.example.EduPOP.interceptor.LoginCheckInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LoginCheckInterceptor loginCheckInterceptor;

    @Override
    public void addInterceptors(
            InterceptorRegistry registry
    ) {
        registry.addInterceptor(loginCheckInterceptor)
                .addPathPatterns(
                        "/account/**",
                        "/student/**",
                        "/teacher/**",
                        "/exam/**",
                        "/classroom/**",
                        "/main/studentMain",
                        "/main/teacherMain",
                        "/main/adminMain",
                        "/blankPage",
                        "/adminWaiting",
                        "/selectAcademy"
                );
    }
}