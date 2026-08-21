package com.example.EduPOP.controller.auth;

import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.service.auth.KakaoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthControllerKakao {
    private final KakaoService kakaoService;

    @GetMapping("/")
    public String mainHome(){
        return "mainPage";
    }
    //카카오 로그인 버튼 누르면 카카오로 안내
    @GetMapping("/kakao/login")
    public String kakaoLogin(){
        //발급받은 키 삽입
        String kakaoAddress = "https://kauth.kakao.com/oauth/" +
                "authorize?client_id=f0d17d7cf78033e1ed7f979b9b09591b" +
                "&redirect_uri=http://localhost:8080/kakao/callback&response_type=code"+
                "&prompt=login";
        return "redirect:"+kakaoAddress;
    }

    //user가 인증코드 들고 옴
    @GetMapping("/kakao/callback")
    public String kakaoCallback(@RequestParam String code, RedirectAttributes redirectAttributes, HttpSession session){
        //카카오 로그인 완료 및 회원 정보 가져오기
        User kakaoUser = kakaoService.loginWithKakao(code);
        //세션에 로그인 정보 저장
        session.setAttribute("loginUser",kakaoUser);
        return "redirect:/blankPage";
    }
    @GetMapping("/blankPage")
    public String adminMain() {
        return "main/blankPage/blankPage";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session){
        //세션 지움
        session.invalidate();
        return "redirect:/";
    }
}
