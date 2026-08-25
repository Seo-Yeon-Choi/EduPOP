package com.example.EduPOP.repository.exp;

import com.example.EduPOP.domain.exp.ExpGrowth;
import com.example.EduPOP.domain.exp.ExpLog;
import com.example.EduPOP.domain.exp.ExpRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper // MyBatis가 경험치를 DB 작업용 객체로 자동으로 생성
public interface ExpMapper { //  경험치 관련 DB 작업 규칙

    ExpRule findExpRuleByActivityType(
            @Param("activityType") String activityType
    ); // 활동에 맞는 경험치 지급 룰 조회

    ExpGrowth findExpGrowthByStudentId(
            @Param("studentId") Long studentId
    ); // 학생 번호로 누적 경험치와 캐릭터 등급 조회

    int insertExpGrowth(
            ExpGrowth expGrowth
    ); // 처음 경험치를 받는 학생의 성장 기록 등록

    int addExp( //  학생의 기존 경험치에 새 경험치 추가
            @Param("studentId") Long studentId,
            @Param("earnedExp") Integer earnedExp
    );

    int updateExpStage(
            @Param("studentId") Long studentId,
            @Param("characterStage") Integer characterStage
    ); // 경험치에 맞게 학생의 캐릭터 단계 수정

    int countDuplicateExp(
            @Param("studentId") Long studentId,
            @Param("activityType") String activityType,
            @Param("referenceId") Long referenceId
    ); // (듀플리케이트): 같은 활동의 경험치 중복 지급 확인

    int insertExpLog(
            ExpLog expLog
    ); // 경험치 지급 기록 등록

    List<ExpLog> findExpLogsByStudentId(
            @Param("studentId") Long studentId
    ); // 학생 번호로 경험치 지급 로그 목록 조회
}