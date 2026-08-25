package com.example.EduPOP.controller.classroom.dto;

import com.example.EduPOP.domain.classroom.Classroom;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * [WBS 1.2.1] 신규 반 개설 요청 전용 DTO (Data Transfer Object)
 *
 * [설계 및 도입 배경]
 * 1. 보안 강화 (Mass Assignment 차단):
 *    - 도메인 엔티티(Classroom)를 직접 화면에 노출하지 않고 입력 전용 바구니를 분리하여,
 *      클라이언트가 임의의 내부 필드(status, createdAt 등)를 변조 주입하는 것을 원천 방지합니다.
 *
 * 2. 서버 사이드 유효성 검증 (Bean Validation):
 *    - 프론트엔드의 required 속성은 개발자 도구 조작 등으로 손쉽게 우회될 수 있으므로,
 *      서버 진입점(@Valid)에서 필수값, 문자열 길이, 수치 범위를 정밀 검증하여 데이터 무결성을 보장합니다.
 */
@Data
@NoArgsConstructor
public class ClassroomCreateRequest {

    /**
     * 소속 학원 식별 번호 (FK)
     * - 필수값 검증: 어느 학원에 속한 반인지 반드시 전달되어야 함
     */
    @NotNull(message = "소속 학원 정보가 올바르지 않습니다. 다시 로그인해 주세요.")
    private Long academyId;

    /**
     * 반 명칭
     * - @NotBlank: null, 빈 문자열(""), 공백(" ")을 모두 허용하지 않음
     * - @Size: DB 컬럼(VARCHAR(100)) 규격에 맞춰 최대 100자 제한
     */
    @NotBlank(message = "개설할 반 이름을 입력해 주세요.")
    @Size(max = 100, message = "반 명칭은 100자 이하로 입력해 주세요.")
    private String name;

    /**
     * 대상 학년 (예: 초5, 중2, 고1 등)
     * - @NotBlank: 필수 입력 항목
     * - @Size: DB 컬럼(VARCHAR(30)) 규격에 맞춰 최대 30자 제한
     */
    @NotBlank(message = "대상 학년은 필수입니다.")
    @Size(max = 30, message = "대상 학년은 30자 이하로 입력해 주세요.")
    private String targetGrade;

    /**
     * 최대 수강 가능 정원 (명)
     * - @NotNull: 필수 숫자 입력
     * - @Min(1): 정원은 최소 1명 이상이어야 비즈니스 규칙에 부합
     */
    @NotNull(message = "수강 인원 정원을 입력해 주세요.")
    @Min(value = 1, message = "수강 정원은 최소 1명 이상이어야 합니다.")
    private Integer maxStudents;

    /**
     * 반 소개 및 운영 계획 설명 (선택 항목)
     * - @Size: DB 컬럼(VARCHAR(255)) 용량 초과 방지
     */
    @Size(max = 255, message = "반 설명은 255자 이하로 입력해 주세요.")
    private String description;

    /**
     * 배정될 담당 강사진 목록 (N:M 매핑용 리스트)
     * - @Valid: 리스트 내부의 각 TeacherRequest 객체 속성까지 연쇄(Cascade) 검증 수행
     * - 초기화: 폼 바인딩 시 NullPointerException을 방지하기 위해 빈 ArrayList로 생성
     */
    @Valid
    private List<TeacherRequest> teachers = new ArrayList<>();

    /**
     * 반 개설 요청 내 강사 매핑 정보 전용 Inner DTO
     */
    @Data
    @NoArgsConstructor
    public static class TeacherRequest {

        /**
         * 강사 고유 사용자 ID (users 테이블 PK)
         */
        @NotNull(message = "담당 강사를 선택해 주세요.")
        private Long teacherId;

        /**
         * 담당 역할 (도메인 Inner Enum 매핑: MAIN(담임) / SUB(부담임))
         * - 미입력 시 기본값으로 부담임(SUB) 역할 부여
         */
        private Classroom.TeacherRoleType roleType = Classroom.TeacherRoleType.SUB;
    }
}