package com.example.EduPOP.component;

import com.example.EduPOP.domain.user.User;
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
                             Object handler){
        HttpSession session = request.getSession();
        User loginUser = (User) session.getAttribute("loginUser");

        if (loginUser != null){
            User user = userMapper.findById(loginUser.getUser_id());
            if (user != null){
                session.setAttribute("loginUser", user);
            }
        }
        return true;
    }


}
