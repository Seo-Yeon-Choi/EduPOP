package com.example.EduPOP.service.auth;

import com.example.EduPOP.domain.user.Academy;
import com.example.EduPOP.domain.user.UserRole;
import com.example.EduPOP.domain.user.UserStatus;
import com.example.EduPOP.repository.user.AcademyMapper;
import com.example.EduPOP.repository.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor

public class AcademyService {
    private final UserMapper userMapper;
    private final AcademyMapper academyMapper;

    @Transactional
    public void registerAcademy(Academy academy, Long user_id){
        //학원 정보 DB에 저장
        academyMapper.save(academy);
        //방금 생성된 학원번호를 관리자 회원 정보에 업데이트, 상태 ACTIVE로 업데이트
        userMapper.updateAcademyAndStatus(
                user_id,
                academy.getAcademy_id(),
                UserRole.ADMIN,
                UserStatus.ACTIVE);
    }
}
