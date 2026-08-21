package com.example.EduPOP.controller.auth;

import com.example.EduPOP.DTO.ApiResponse;
import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.domain.user.UserRole;
import com.example.EduPOP.domain.user.UserStatus;
import com.example.EduPOP.service.auth.InvitationGenerateService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// @RestController: 이 클래스가 데이터를 HTML 화면이 아닌 JSON 형태로 반환하는 컨트롤러임을 명시
// @RequestMapping: 이 컨트롤러의 모든 주소 앞에 "/api/invitation"을 기본 경로로 설정
@RestController
@RequestMapping("/api/invitation")
@RequiredArgsConstructor
public class InvitationGenerateController {

    private final InvitationGenerateService generateService;

    @PostMapping("/generate")
    //ResponseEntity : 스프링 기본 제공 클래스 - HTTP 상태 코드 덧붙여서 클라이언트에게 반환해줌
    //프론트엔드에서 targetRole을 보내주면 세션을 확인해서 권한 검사 후 ApiResponse에 담아 상태 코드와 함께 반환
    public ResponseEntity<ApiResponse<String>> generateLink(@RequestParam UserRole targetRole, HttpSession session)
    {
        // 세션에서 현재 로그인한 User 객체 정보 가져옴
        User loginUser = (User) session.getAttribute("loginUser");

        // 인증 및 상태 검증
        // 로그인하지 않았거나 상태가 ACTIVE가 아닌 경우 접근 차단
        if (loginUser == null || loginUser.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException("정상적인 계정으로 로그인 후 이용해주세요.");
        }

        // 세션에서 꺼낸 로그인 유저 정보와 초대할 대상의 역할을 Service 계층으로 전달하여 실제 링크 생성 요청
        String inviteLink = generateService.generateInviteLink(loginUser, String.valueOf(targetRole));

        // HTTP 상태 코드 200(OK)과 함께, 생성된 링크를 ApiResponse 포맷에 담아 반환
        return ResponseEntity.ok(
                ApiResponse.success("초대 링크가 성공적으로 발급되었습니다.", inviteLink)
        );
    }
}
