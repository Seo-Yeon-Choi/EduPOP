package com.example.EduPOP.dto.exp;

import lombok.Data;

@Data // Getter, Setter, toString 등 데이터 관리 메서드 자동 생성
public class ExpDTO { // ExpDTO(이엑스피 디티오): 메인 화면에 전달할 성장 정보 객체

    private Long studentId;
    // studentId(스튜던트 아이디): 경험치 정보를 조회한 학생 번호

    private Integer totalExp;
    // totalExp(토탈 이엑스피): 학생의 현재 누적 경험치

    private Integer characterStage;
    // characterStage(캐릭터 스테이지): 현재 캐릭터 성장 단계

    private String stageName;
    // stageName(스테이지 네임): G부터 GROW UP까지의 현재 단계 이름

    private Integer expToNextStage;
    // expToNextStage(이엑스피 투 넥스트 스테이지): 다음 단계까지 필요한 경험치

    private Integer expProgressPercent;
    // expProgressPercent(이엑스피 프로그레스 퍼센트): 현재 단계의 경험치 진행률

    private Boolean maxStage;
    // maxStage(맥스 스테이지): 현재 캐릭터가 최종 6단계인지 저장

    private String characterImageUrl;
    // characterImageUrl(캐릭터 이미지 유알엘): 움직일 캐릭터 이미지 주소

    private String backgroundImageUrl;
    // backgroundImageUrl(백그라운드 이미지 유알엘): 현재 단계의 고정 배경 이미지 주소
}
