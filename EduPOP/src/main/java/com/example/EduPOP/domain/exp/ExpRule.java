package com.example.EduPOP.domain.exp;

import lombok.Data;

import java.time.LocalDateTime;

@Data //게터,세터,투 스트링
public class ExpRule { // (룰): 경험치 지급과 규칙

    private Long expRuleId; // ( 룰): 경험치 규칙 번호

    private String activityType; //(액티비티 ): 경험치 지급 활동 종류

    private Integer expAmount; // ( 어마운트): 경험치 지급할 양

    private Boolean enabled; // (인에이블): 현재 규칙 사용 여부

    private LocalDateTime createdAt; // (크리에이티드 앳): 규칙을 처음 만든 시간

    private LocalDateTime updatedAt; // (업데이트드 앳): 규칙을 마지막으로 수정한 시간
}