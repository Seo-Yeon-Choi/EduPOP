package com.example.EduPOP.controller.classroom.dto;

import com.example.EduPOP.domain.classroom.Classroom;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ClassroomUpdateRequest {
    /**
     * 수정할 반 명칭 (필수값, 최대 100자)
     */
    @NotBlank(message = "반 이름을 입력해 주세요.")
    @Size(max = 100, message = "반 명칭은 100자 이하로 입력해 주세요.")
    private String name;

    /**
     * 대상 학년 (필수값, 최대 30자)
     */
    @NotBlank(message = "대상 학년을 선택하거나 입력해 주세요.")
    @Size(max = 30, message = "대상 학년은 30자 이하로 입력해 주세요.")
    private String targetGrade;

    /**
     * 수강 정원 (필수값, 최소 1명 이상)
     */
    @NotNull(message = "수강 정원을 입력해 주세요.")
    @Min(value = 1, message = "수강 정원은 최소 1명 이상이어야 합니다.")
    private Integer maxStudents;

    /**
     * 반 운영 상태 (ACTIVE: 운영중 / CLOSED: 종강)
     */
    @NotNull(message = "운영 상태를 지정해 주세요.")
    private Classroom.ClassStatus status;

    /**
     * 반 상세 설명 및 메모 (최대 255자)
     */
    @Size(max = 255, message = "반 설명은 255자 이하로 입력해 주세요.")
    private String description;
}
