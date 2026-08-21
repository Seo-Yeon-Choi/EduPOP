package com.example.EduPOP.repository;

import com.example.EduPOP.domain.report.StudentReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentReportRepository extends JpaRepository<StudentReport, Long> {
}