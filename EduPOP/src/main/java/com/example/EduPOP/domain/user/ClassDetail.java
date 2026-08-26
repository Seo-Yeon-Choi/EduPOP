package com.example.EduPOP.domain.user;

import lombok.Data;

import java.util.List;
//관리자페이지 -> 학생관리에 띄울 클래스 정보들
@Data
public class ClassDetail {
    private Long classId;
    private Long academyId;
    private String className;

    private String teacherName;

    private List<StudentInfo> students; //학생명단
}
