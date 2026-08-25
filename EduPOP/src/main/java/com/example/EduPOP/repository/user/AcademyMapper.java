package com.example.EduPOP.repository.user;

import com.example.EduPOP.domain.user.Academy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AcademyMapper {

    // 학원 등록
    void save(Academy academy);

    // 모든 학원 조회
    List<Academy> findAllAcademies();

    // 학원 번호로 특정 학원 조회
    Academy findById(@Param("academyId") Long academyId);

    // 수정
    void updateAcademy(
            @Param("academyId") Long academyId,
            @Param("name") String name,
            @Param("address") String address,
            @Param("phone") String phone,
            @Param("businessCer") String businessCer
    );

    // 소속 유저들의 academyId를 NULL로 비우기 (외래키 제약조건 충돌 방지)
    void clearUserAcademyId(@Param("academyId") Long academyId);

    // 삭제
    void deleteAcademy(@Param("academyId") Long academyId);
}
