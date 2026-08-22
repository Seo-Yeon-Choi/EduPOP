package com.example.EduPOP.repository.user;

import com.example.EduPOP.domain.user.Academy;
import com.example.EduPOP.domain.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AcademyMapper {
    //저장
    void save(Academy academy);
    //모든 학원 조회
    List<Academy> findAllAcademies();
    //삭제
    void deleteAcademy(@Param("academy_id") Long academy_id);
    //수정
    void updateAcademy(
            @Param("academy_id") Long academy_id,
            @Param("name") String name,
            @Param("address") String address,
            @Param("phone") String phone,
            @Param("business_cer") String business_cer
    );
}
