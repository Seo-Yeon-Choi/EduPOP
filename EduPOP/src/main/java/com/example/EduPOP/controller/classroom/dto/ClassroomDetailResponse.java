package com.example.EduPOP.controller.classroom.dto;

import com.example.EduPOP.domain.classroom.Classroom;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


// 화면(Response) 전용 내부 DTO를 사용하는 이유
// User와 ClassTeacher를 JOIN한 알짜배기 정보(ID + 강사명 + 역할)만 묶어서 깔끔하게 전달
@Data
@NoArgsConstructor
public class ClassroomDetailResponse {
    private Long classId;
    private Long academyId;
    private String name;
    private String targetGrade;
    private Integer maxStudents;
    private Classroom.ClassStatus status;
    private String description;
    private LocalDateTime createdAt;

    private List<TeacherInfo> teachers = new ArrayList<>();

    private List<StudentInfo> students = new ArrayList<>();

    @Data
    @NoArgsConstructor
    public static class TeacherInfo {
        private Long teacherId; // users.user_id
        private String name; // users.name (도메인 ClassTeacher에는 없는 필드)
        private String roleType; // class_teachers.role_type (도메인 User에는 없는 필드)
    }


    @Data
    @NoArgsConstructor
    public static class StudentInfo {
        private Long studentId;
        private String name;
        private String grade;            // school_grade
        private String phone;            // 연락처
        private String currentClassName; // 전반 확인용 (타 반 소속명)
        private LocalDateTime enrolledAt;

        /**
         * 현재 반 배정 명단용 식별 라벨
         * 출력 예시: [중2] 김민수 (1111 | 기초수학반)
         */
        public String getDisplayLabel() {
            String gradePrefix = (grade != null && !grade.isBlank())
                    ? "[" + grade + "] "
                    : "";

            String phonePart = (phone != null && phone.length() >= 4)
                    ? " (" + phone.substring(phone.length() - 4) + ")"
                    : "";

            return String.format("%s%s%s", gradePrefix, name, phonePart);
        }

    }
}


