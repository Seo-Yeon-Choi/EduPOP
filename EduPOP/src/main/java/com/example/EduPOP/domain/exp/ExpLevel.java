package com.example.EduPOP.domain.exp;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter // final 변수의 Getter를 자동 생성
@RequiredArgsConstructor // final 변수를 받는 생성자를 자동 생성
public enum ExpLevel { // ExpLevel(이엑스피 레벨): 경험치에 맞는 성장 단계 기준

    LEVEL_1(1, 0, "G"), // 경험치 0점 이상이면 캐릭터 1단계

    LEVEL_2(2, 100, "GR"), // 경험치 100점 이상이면 캐릭터 2단계

    LEVEL_3(3, 300, "GRO"), // 경험치 300점 이상이면 캐릭터 3단계

    LEVEL_4(4, 600, "GROW"), // 경험치 600점 이상이면 캐릭터 4단계

    LEVEL_5(5, 1000, "GROW U"), // 경험치 1000점 이상이면 캐릭터 5단계

    LEVEL_6(6, 1500, "GROW UP"); // 경험치 1500점 이상이면 캐릭터 최종 6단계


    private final int characterStage;
    // characterStage(캐릭터 스테이지): 캐릭터 성장 단계

    private final int minExp;
    // minExp(민 이엑스피): 해당 단계가 시작되는 최소 경험치

    private final String stageName;
    // stageName(스테이지 네임): 화면에 표시할 단계 이름


    public static ExpLevel findLevel(
            Integer totalExp
    ) { // findLevel(파인드 레벨): 누적 경험치에 맞는 성장 단계 전체 정보 조회

        if (totalExp == null
                || totalExp < LEVEL_2.minExp) {

            return LEVEL_1; // 경험치가 없거나 100점 미만이면 1단계
        }

        if (totalExp < LEVEL_3.minExp) {
            return LEVEL_2; // 경험치가 100점 이상 300점 미만이면 2단계
        }

        if (totalExp < LEVEL_4.minExp) {
            return LEVEL_3; // 경험치가 300점 이상 600점 미만이면 3단계
        }

        if (totalExp < LEVEL_5.minExp) {
            return LEVEL_4; // 경험치가 600점 이상 1000점 미만이면 4단계
        }

        if (totalExp < LEVEL_6.minExp) {
            return LEVEL_5; // 경험치가 1000점 이상 1500점 미만이면 5단계
        }

        return LEVEL_6; // 경험치가 1500점 이상이면 최종 6단계
    }


    public static int findCharacterStage(
            Integer totalExp
    ) { // findCharacterStage(파인드 캐릭터 스테이지): 누적 경험치에 맞는 단계 번호 조회

        return findLevel(
                totalExp
        ).getCharacterStage(); // 조회한 성장 단계에서 단계 번호 반환
    }


    public ExpLevel findNextLevel() {
        // findNextLevel(파인드 넥스트 레벨): 현재 단계의 다음 성장 단계 조회

        return switch (this) {

            case LEVEL_1 -> LEVEL_2; // 1단계 다음은 2단계

            case LEVEL_2 -> LEVEL_3; // 2단계 다음은 3단계

            case LEVEL_3 -> LEVEL_4; // 3단계 다음은 4단계

            case LEVEL_4 -> LEVEL_5; // 4단계 다음은 5단계

            case LEVEL_5 -> LEVEL_6; // 5단계 다음은 6단계

            case LEVEL_6 -> null; // 최종 단계이면 다음 단계가 없음
        };
    }
}
