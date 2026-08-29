package com.example.EduPOP.domain.exp;

public enum ExpActivityType { // 경험치 지금 기록 , 경험치 지급 룰

    EXAM_LOG, // : 일반 시험 경험치 지급 로그

    REVIEW_LOG, // : 시험 복습 경험치 지급 로그

    DAILY_REVIEW_LOG, // : 오늘의 복습 경험치 지급 로그

    READING_LOG, // : 독서감상문 경험치 지급 로그


    EXAM_100_RULE, // : 환산 시험 점수가 100점일 때 경험치 50점 룰

    EXAM_80_RULE, // 환산 시험 점수가 80점 이상 100점 미만일 때 경험치 40점 룰

    EXAM_70_RULE, // 환산 시험 점수가 70점 이상 80점 미만일 때 경험치 30점 룰

    EXAM_UNDER_70_RULE, // 환산 시험 점수가 70점 미만일 때 경험치 20점 룰


    REVIEW_RULE, // (리뷰 룰): 다른 시험을 처음 복습했을 때 경험치 30점 룰


    READING_1_RULE, // : 같은 책의 첫 번째 독서감상문 경험치 35점 룰

    READING_2_RULE, // 같은 책의 두 번째 독서감상문 경험치 25점 룰

    READING_3_RULE, // 같은 책의 세 번째 독서감상문 경험치 15점 룰

    READING_4_RULE // 같은 책의 네 번째 독서감상문 경험치 5점 룰
}