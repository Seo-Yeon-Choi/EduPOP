package com.example.EduPOP.repository;

import com.example.EduPOP.domain.report.ParentReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParentReportRepository extends JpaRepository<ParentReport, Long> {

    /**
     * 학생 ID를 기준으로 전체 성적 리포트를 기간(오래된 순) 정렬하여 조회합니다.
     * 차트 페이징(시계열 데이터)을 구성할 때 사용됩니다.
     */
    List<ParentReport> findAllByStudentIdOrderByPeriodEndAsc(Long studentId);
}