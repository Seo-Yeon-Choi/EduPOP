package com.example.EduPOP.domain.exam;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ExamSection {

    private Long sectionId;

    private Long examId;

    private String sectionType;

    private String sectionName;

    private Integer maxScore;

    private Integer sortOrder;

}
