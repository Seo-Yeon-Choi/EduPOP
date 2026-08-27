package com.example.EduPOP.domain.exp;

import lombok.Data;

import java.time.LocalDateTime;

@Data //
public class ExpGrowth { // (그로스): 학생의 누적 경험치와 캐릭터 성장 정보

    private Long studentId; // 경험치를 보유한 학생 번호

    private Integer totalExp; // (토탈): 학생이한테 있는  전체 경험치

    private Integer characterStage; // (캐릭터 스테이지): 현재 캐릭터 등급

    private LocalDateTime updatedAt; // 경험치 또는 캐릭터 등급이 마지막으로 변경된 시간
}