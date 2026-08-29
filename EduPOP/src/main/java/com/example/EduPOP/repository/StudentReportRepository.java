package com.example.EduPOP.repository;

import com.example.EduPOP.domain.report.StudentReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface StudentReportRepository
        extends JpaRepository<StudentReport, Long> {

    // 현재 로그인한 학생의 가장 최근 리포트
    Optional<StudentReport>
    findTopByStudentIdOrderByPeriodEndDesc(
            Long studentId
    );

    // 특정 학생 + 특정 기간 리포트
    Optional<StudentReport>
    findByStudentIdAndPeriodStartAndPeriodEnd(
            Long studentId,
            LocalDate periodStart,
            LocalDate periodEnd
    );

    // 현재 리포트보다 이전 기간의 가장 최근 리포트
    Optional<StudentReport>
    findTopByStudentIdAndPeriodEndBeforeOrderByPeriodEndDesc(
            Long studentId,
            LocalDate periodEnd
    );
}