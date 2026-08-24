package com.example.EduPOP.controller.classroom.dto;

import com.example.EduPOP.domain.classroom.Classroom;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ClassroomListResponse {

    private Long classId;  // 반 고유 식별자 (PK, AUTO_INCREMENT)
    private String name; // 반 명칭 (학원 내 중복 방지 대상)
    private String targetGrade; // 대상 학년
    private Integer maxStudents; // 수강 정원 (정원 초과 방지 기준)
    private String mainTeacherName; // 대표 담임 강사명 (users 테이블 조인 결과)
    private Classroom.ClassStatus status; // 반 운영 상태 (ACTIVE: 운영중, CLOSED: 종강)
    private LocalDateTime createdAt;
}
