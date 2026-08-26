package com.example.EduPOP.domain.exam;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Exam {
    private Long examId;
    private Long classId;
    private Long teacherId;
    private Long templateExamId;
    private String title;
    private String examType;
    private String examMode;
    private Integer examRound;
    private String status;
    private LocalDate examDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}