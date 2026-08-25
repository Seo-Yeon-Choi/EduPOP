package com.example.EduPOP.domain.user;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AcademyClass {
    private Long classId;
    private Long academyId;
    private String name;
    private Integer targetGrade;
    private Integer maxStudents;
    private String status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
