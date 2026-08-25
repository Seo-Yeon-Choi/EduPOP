package com.example.EduPOP.service.auth;

import com.example.EduPOP.domain.user.Academy;
import com.example.EduPOP.domain.user.User;
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

    // 학원 등록
    public void registerAcademy(Academy academy, Long userId) {
        // 학원 정보 DB에 저장
        academyMapper.save(academy);

        // 유저의 역할, 상태, 학원 번호를 DB에 업데이트
        userMapper.updateAcademyAndStatus(
                userId,
                academy.getAcademyId(),
                UserRole.ADMIN,
                UserStatus.ACTIVE
        );
    }

    // 학원번호로 특정 학원 조회
    public Academy getAcademyById(Long academyId) {
        if (academyId == null) {
            return null;
        }

        return academyMapper.findById(academyId);
    }

    // 세션 갱신용 최신 유저 정보 조회
    public User findById(Long userId) {
        return userMapper.findByUserId(userId);
    }

    // 학원 수정
    public void updateAcademy(Academy academy) {
        academyMapper.updateAcademy(
                academy.getAcademyId(),
                academy.getName(),
                academy.getAddress(),
                academy.getPhone(),
                academy.getBusinessCer()
        );
    }

    // 학원 삭제(user의 academyId를 null로 비운 후 삭제)
    public void deleteAcademy(Long academyId) {
        academyMapper.clearUserAcademyId(academyId);
        academyMapper.deleteAcademy(academyId);
    }

    //전체 학원을 가져옴
    public List<Academy> getAllAcademies() {
        return academyMapper.findAllAcademies();
    }
}

