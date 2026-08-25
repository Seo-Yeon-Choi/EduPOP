package com.example.EduPOP.domain.classroom;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AcademyClass {

    private Long classId;
    private Long academyId;

    private String name;
    private String targetGrade;

    private Integer maxStudents;

    private String status;
    private String description;
}