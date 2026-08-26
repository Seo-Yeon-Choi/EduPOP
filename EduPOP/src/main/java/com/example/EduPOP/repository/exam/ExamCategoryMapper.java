package com.example.EduPOP.repository.exam;

import com.example.EduPOP.controller.exam.dto.ExamCategoryDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExamCategoryMapper {
    // 구분(EXAM_TYPE, LARGE, SMALL)별 목록 조회
    List<ExamCategoryDto> findCategoriesByType(@Param("categoryType") String categoryType);

    //새로운 분류 항목 추가
    void insertCategory(ExamCategoryDto categoryDto);

    // 분류 항목 삭제
    void deleteCategory(@Param("categoryId") Long categoryId);
}