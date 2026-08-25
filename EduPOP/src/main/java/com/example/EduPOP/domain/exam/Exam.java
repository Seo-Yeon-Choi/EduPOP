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


    /*
     * DB 컬럼은 아니지만
     * Java에서 시험 전체를 다룰 때 사용
     */
    private List<ExamSection> sections = new ArrayList<>();

    private List<ExamQuestion> questions = new ArrayList<>();

}
