package com.example.EduPOP.service.auth;

import com.example.EduPOP.config.SessionConst;
import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.domain.user.UserRole;
import com.example.EduPOP.domain.user.UserStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SecurityLoginService {

    private final SecurityContextRepository securityContextRepository;

    public void login(
            User user,
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        HttpSession session = request.getSession(true);

        // Session Fixation 방지
        request.changeSessionId();

        List<GrantedAuthority> authorities =
                new ArrayList<>();

        /*
         * ACTIVE 사용자만 실제 Role 권한 부여
         *
         * PENDING 사용자는 로그인 자체는 되어 있지만
         * /student/**, /teacher/**, /admin/**
         * 접근은 불가능하게 한다.
         */
        if (user.getStatus() == UserStatus.ACTIVE
                && user.getRole() != null
                && user.getRole() != UserRole.NONE) {

            authorities.add(
                    new SimpleGrantedAuthority(
                            "ROLE_" + user.getRole().name()
                    )
            );
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        authorities
                );

        SecurityContext context =
                SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);

        // Spring Security 세션에 인증정보 저장
        securityContextRepository.saveContext(
                context,
                request,
                response
        );

        // 기존 EduPOP 코드와의 호환을 위해 당분간 유지
        session.setAttribute(
                SessionConst.LOGIN_USER,
                user
        );
    }
}