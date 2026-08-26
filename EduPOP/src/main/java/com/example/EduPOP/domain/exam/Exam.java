package com.example.EduPOP.domain.exam;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Exam {
    private Long examId;
    private Long classId;
    private String className;
    private Long teacherId;
    private Long templateExamId;
    private String title;
    private String examType;
    private ExamMode examMode;
    private Integer examRound;
    private ExamStatus status;
    private LocalDate examDate;
    private String fileUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ExamSection> sections = new ArrayList<>();
    private List<ExamQuestion> questions = new ArrayList<>();
}