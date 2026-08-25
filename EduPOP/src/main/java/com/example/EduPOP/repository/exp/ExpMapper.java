package com.example.EduPOP.repository.exp;

import com.example.EduPOP.domain.exp.ExpGrowth;
import org.apache.ibatis.annotations.Mapper;

@Mapper // MyBatis가 구현 객체를 자동으로 만들어 관리
public interface ExpMapper {

    ExpGrowth findExpGrowthByStudentId(
            Long studentId
    ); // 학생 번호로 성장 정보 조회
}