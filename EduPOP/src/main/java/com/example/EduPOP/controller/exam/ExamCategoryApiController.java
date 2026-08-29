package com.example.EduPOP.controller.exam;

import com.example.EduPOP.controller.exam.dto.ExamCategoryDto;
import com.example.EduPOP.repository.exam.ExamCategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/exam/api/categories")
@RequiredArgsConstructor
public class ExamCategoryApiController {

    private final ExamCategoryMapper categoryMapper;

    // 1. 시험 등록 화면의 토글에 띄워줄 전체 분류 목록 조회 API
    @GetMapping
    public ResponseEntity<Map<String, List<ExamCategoryDto>>> getAllCategories() {
        Map<String, List<ExamCategoryDto>> result = new HashMap<>();
        result.put("examTypes", categoryMapper.findCategoriesByType("EXAM_TYPE"));
        result.put("largeCategories", categoryMapper.findCategoriesByType("LARGE"));
        result.put("smallCategories", categoryMapper.findCategoriesByType("SMALL"));
        return ResponseEntity.ok(result);
    }

    // 2. 선생님이 새로운 분류(시험유형/대분류/소분류)를 추가하는 API
    @PostMapping
    public ResponseEntity<Void> addCategory(@RequestBody ExamCategoryDto requestDto) {
        categoryMapper.insertCategory(requestDto);
        return ResponseEntity.ok().build();
    }

    // 3. 분류 항목 삭제 API
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long categoryId) {
        categoryMapper.deleteCategory(categoryId);
        return ResponseEntity.ok().build();
    }
}