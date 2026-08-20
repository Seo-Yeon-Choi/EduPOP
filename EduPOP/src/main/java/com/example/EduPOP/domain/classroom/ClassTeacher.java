package com.example.EduPOP.domain.classroom;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 반-강사 매핑 (class_teachers 테이블 매핑, N:M 관계)
 */
@Data
@NoArgsConstructor
public class ClassTeacher {
    private Long classTeacherId; // 매핑 PK
    private Long classId; // 반 FK
    private Long teacherId; // 강사 FK
    private Classroom.TeacherRoleType roleType; // Enum 적용 (담당 역할)
    private LocalDateTime createdAt;

    public void changeRole(Classroom.TeacherRoleType roleType){
        this.roleType = roleType;
    }
}
