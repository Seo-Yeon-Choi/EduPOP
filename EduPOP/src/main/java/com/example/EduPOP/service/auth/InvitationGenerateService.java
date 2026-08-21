//package com.example.EduPOP.service.auth;
//
//import com.example.EduPOP.component.TokenProvider;
//import com.example.EduPOP.domain.user.User;
//import com.example.EduPOP.domain.user.UserRole;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class InvitationGenerateService {
//
//    private final TokenProvider tokenProvider;
//
//    //requester-요청한 사람, targetRole-초대받을 사람
//    public String generateInviteLink(User requester, String targetRole) {
//        // 발급 권한 자격 검증 로직
//        validateInvitationPermission(requester.getRole(), UserRole.valueOf(targetRole));
//
//        // 검증 통과 시 requester의 학원번호와 targetRole을 넣어서 토큰 생성
//        String token = tokenProvider.createInvitationToken(requester.getAcademy_id(), targetRole);
//        //로그에 남김
//        log.info("초대 링크 발급 성공 - 발급자 ID: {}, 대상 역할: {}", requester.getUser_id(), targetRole);
//
//        return "https://localhost:8080.com/invite?token=" + token;
//    }
//
//    // 발급자의 권한과 초대 대상의 권한을 비교하여 접근을 제어
//    private void validateInvitationPermission(UserRole requesterRole, UserRole targetRole) {
//        if (requesterRole == UserRole.ADMIN) {
//            return; // 관리자는 모든 역할 초대 가능
//        }
//
//        if (requesterRole == UserRole.TEACHER && targetRole == UserRole.STUDENT) {
//            return; // 강사는 학생만 초대 가능
//        }
//
//        // 그 외의 경우 (예: 학생이 초대 시도, 강사가 강사 초대 시도) 예외 발생
//        log.warn("권한 탈취 시도 혹은 잘못된 접근 - 요청자 권한: {}, 초대 대상 권한: {}", requesterRole, targetRole);
//        throw new IllegalArgumentException("해당 역할을 초대할 권한이 없습니다.");
//    }
//}