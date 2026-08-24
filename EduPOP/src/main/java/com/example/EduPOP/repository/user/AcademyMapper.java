package com.example.EduPOP.repository.user;

import com.example.EduPOP.domain.user.Academy;
import com.example.EduPOP.domain.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Mapper
public interface AcademyMapper {
    //학원 등록
    void save(Academy academy);

    //모든 학원 조회
    List<Academy> findAllAcademies();

    //학원 번호로 특정학원 조회
    Academy findById(@Param("academy_id") Long academy_id);

    //수정
    void updateAcademy(
            @Param("academy_id") Long academy_id,
            @Param("name") String name,
            @Param("address") String address,
            @Param("phone") String phone,
            @Param("business_cer") String business_cer
    );


    //소속 유저들의 academy_id를 NULL로 비우기 (외래키 제약조건 충돌 방지)
    void clearUserAcademyId(@Param("academy_id") Long academy_id);
    //삭제
    void deleteAcademy(@Param("academy_id") Long academy_id);

}
