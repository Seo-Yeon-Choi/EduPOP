package com.example.EduPOP.service.auth;

import com.example.EduPOP.domain.user.Academy;
import com.example.EduPOP.domain.user.UserRole;
import com.example.EduPOP.domain.user.UserStatus;
import com.example.EduPOP.repository.user.AcademyMapper;
import com.example.EduPOP.repository.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
                UserStatus.ACTIVE
        );
    }
    //학원 조회
    public List<Academy> getAllAcademies(){
        return academyMapper.findAllAcademies();
    }
    //삭제
    public void deleteAcademy(Long academy_id){
        academyMapper.deleteAcademy(academy_id);
    }
    //수정
    public void updateAcademy(Long academy_id, String name, String address, String phone, String business_cer){
        academyMapper.updateAcademy(academy_id, name, address, phone, business_cer);
    }
}
