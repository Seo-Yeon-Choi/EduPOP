package com.example.EduPOP.domain.exp;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExpLog { // (exp 로그): 경험치 지급 기록

    private Long logId; // (로그 ): 경험치 지급 기록 번호

    private Long studentId; // 경험치를 지급받은 학생 번호

    private String activityType; // 경험치를 지급받은 활동 종류

    private Long referenceId; // 경험치를 발생시킨 시험·복습·독서감상문 번호 저장

    private Integer earnedExp; // (언드 ): 학생에게 실제로 지급된 경험치

    private LocalDateTime createdAt; // 경험치가 지급된 시간 저장
}