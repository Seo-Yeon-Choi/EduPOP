package com.example.EduPOP.domain.exam;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Exam {

    private Long examId;
    private Long classId;
    private String className;
    private Long teacherId;
    private Long templateExamId;
    private String title;
    private ExamType examType;
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
