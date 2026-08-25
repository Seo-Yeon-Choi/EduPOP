package com.example.EduPOP.domain.classroom;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 반-수강생 매핑 (class_students 테이블 매핑)
 */
@Data
@NoArgsConstructor
public class ClassStudent {
    private Long classStudentId; // 매핑 PK
    private Long classId; // 반 FK
    private Long studentId; // 학생 FK
    private Classroom.ClassStudentStatus status; // Enum 적용 (수강 상태)
    private LocalDateTime createdAt;

    // 정상 수강 활성화
    public void activate() {
        this.status = Classroom.ClassStudentStatus.ACTIVE;
    }

    // 다른 반으로 전반(이동) 처리
    public void transfer() {
        this.status = Classroom.ClassStudentStatus.TRANSFERRED;
    }

    // 반에서 퇴원/제외 처리
    public void withdraw() {
        this.status = Classroom.ClassStudentStatus.DROPPED;
    }
}
