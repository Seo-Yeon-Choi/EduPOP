package com.example.EduPOP.controller.exam.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExamCategoryDto {
    private Long categoryId;
    private Long academyId;
    private String categoryType;  // 'EXAM_TYPE', 'LARGE', 'SMALL'
    private String categoryName;  // 명칭
    private Integer sortOrder;
}