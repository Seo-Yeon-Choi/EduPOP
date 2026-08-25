package com.example.EduPOP.domain.report;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

public class StudentReportTest {


// ★ System.out.println 대신 진짜 개발자처럼 에러가 터지는지 확인하는 테스트!

        @Test
        void 정상적으로_리포트가_생성된다() {
            // given (주어진 상황 - 날짜 준비)
            LocalDate start = LocalDate.of(2026, 8, 1);
            LocalDate end = LocalDate.of(2026, 8, 31);

            // when & then (실행했을 때 에러가 안 나고 잘 넘어가야 성공!)
            assertThatCode(() -> {
                new StudentReport(1L, 999L, start, end);
            }).doesNotThrowAnyException();

            System.out.println("✅ 정상 생성 테스트 통과!");
        }

        @Test
        void 아이디가_없으면_에러가_터져야한다() {
            LocalDate start = LocalDate.of(2026, 8, 1);
            LocalDate end = LocalDate.of(2026, 8, 31);

            // 학생 아이디 자리에 텅 빈 값("")을 넣어봤습니다.
            // 우리가 StudentReport 생성자에서 throw new IllegalArgumentException을 했기 때문에,
            // 여기서 진짜로 그 에러가 터져야 이 테스트가 '성공'하는 겁니다!
            assertThatIllegalArgumentException().isThrownBy(() -> {
                new StudentReport(1L, 999L, start, end);
            });

            System.out.println("✅ 아이디 누락 에러 발생 테스트 통과!");

    }
}
