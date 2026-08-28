package com.example.EduPOP.interceptor;

import com.example.EduPOP.config.SessionConst;
import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.domain.user.UserRole;
import com.example.EduPOP.domain.user.UserStatus;
import com.example.EduPOP.repository.user.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
@RequiredArgsConstructor

public class LoginCheckInterceptor implements HandlerInterceptor {
    private final UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception{
        HttpSession session = request.getSession(false);
        User loginUser =
                session == null ? null : (User) session.getAttribute(SessionConst.LOGIN_USER);
        String requestUri = request.getRequestURI();

        if (loginUser == null){
            response.sendRedirect("/");
            return false;
        }

        if(loginUser.getStatus() == UserStatus.WITHDRAWN){
            session.invalidate();
            response.sendRedirect("/");
            return false;
        }

        String uri = request.getRequestURI();

        // Role 검사해서 url로 접근 차단
        if (uri.startsWith("/student/")
                && loginUser.getRole() != UserRole.STUDENT) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN
            );

            return false;
        }

        if ((uri.startsWith("/teacher/")
                || uri.startsWith("/exam/"))
                && loginUser.getRole() != UserRole.TEACHER) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN
            );

            return false;
        }

        if (uri.startsWith("/admin/")
                && loginUser.getRole() != UserRole.ADMIN) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN
            );

            return false;
        }

        // 메인 화면을 url로 접근하는거 차단

        if (uri.equals("/main/studentMain")
                && loginUser.getRole() != UserRole.STUDENT) {
            response.sendError(403);
            return false;
        }

        if (uri.equals("/main/teacherMain")
                && loginUser.getRole() != UserRole.TEACHER) {
            response.sendError(403);
            return false;
        }

        if (uri.equals("/main/adminMain")
                && loginUser.getRole() != UserRole.ADMIN) {
            response.sendError(403);
            return false;
        }

        return true;
    }


}