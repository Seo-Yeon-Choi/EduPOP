//package com.example.EduPOP.service.auth;
//
//import com.example.EduPOP.component.TokenProvider;
//import com.example.EduPOP.domain.user.User;
//import com.example.EduPOP.domain.user.UserRole;
//import com.example.EduPOP.domain.user.UserStatus;
//import com.example.EduPOP.repository.user.UserMapper;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//@Service
//@RequiredArgsConstructor
//public class InvitationService {
//    private final UserMapper userMapper;
//    private final TokenProvider tokenProvider;
//
//    @Transactional
//    public User processInvitation(String token, Long user_id){
//        //토큰이 가짜이거나 기한이 지난 거라면
//        if (!tokenProvider.validateToken(token)){
//            throw new IllegalArgumentException("유효하지 않거나 만료된 초대권입니다.");
//        }
//
//        // 토큰이 진짜라면 학원 번호와 역할 꺼내옴
//        Long academy_id = tokenProvider.getAcademyIdFromToken(token);
//        UserRole assignedRole = tokenProvider.getRoleFromToken(token);
//
//        // DB에 업데이트
//        userMapper.updateAcademyAndStatus(user_id, academy_id, assignedRole, UserStatus.ACTIVE);
//
//        // 업데이트 된 user_id 반환
//        return userMapper.findById(user_id);
//    }
//
//}
