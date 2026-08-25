package com.example.EduPOP.service;

import com.example.EduPOP.domain.report.ParentReport;
import com.example.EduPOP.dto.ReportMetricsDTO;
import com.example.EduPOP.mapper.ReportMapper;
import com.example.EduPOP.repository.ParentReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ParentReportService {
    private final ParentReportRepository repository;
    private final ReportMapper reportMapper;

    public ParentReportService(ParentReportRepository repository, ReportMapper reportMapper) {
        this.repository = repository;
        this.reportMapper = reportMapper;
    }

    /**
     * 학부모 리포트 단건 조회 및 사용자 정보 결합
     */
    @Transactional(readOnly = true)
    public ParentReport getReport(Long reportId) {
        ParentReport report = repository.findById(reportId).orElse(null);

        if (report != null) {
            // Report 테이블에는 ID만 존재하므로, Mapper를 통해 실제 이름을 조회하여 병합
            String studentName = reportMapper.getUserNameById(report.getStudentId());
            String teacherName = reportMapper.getUserNameById(report.getCreatedBy());
            String className = reportMapper.getClassNameByStudentId(report.getStudentId());

            report.setStudentInfo(
                    studentName != null ? studentName : "알 수 없는 학생",
                    className != null ? className : "소속 반 없음"
            );
            report.setTeacherName(teacherName != null ? teacherName : "담당 선생님 미정");
        }
        return report;
    }

    /**
     * 교사 발행용 리포트 스냅샷 생성 및 저장
     */
    @Transactional
    public ParentReport generateReportFromDB(Long studentId, Long teacherId, LocalDate startDate, LocalDate endDate) {
        ReportMetricsDTO metrics = reportMapper.getMonthlyReportMetrics(studentId, teacherId, startDate, endDate);

        // TODO: 향후 RadarChart API를 연동하여 실제 데이터를 받아오도록 수정 필요
        String radarJson = "{\"듣기\": 80.0, \"쓰기\": 65.0, \"어휘\": 90.0, \"문법\": 48.0, \"독해\": 92.0}";

        ParentReport report = new ParentReport(studentId, teacherId, startDate, endDate);

        if (metrics != null) {
            report.setStudentInfo(metrics.getStudentName(), metrics.getClassName());
            report.setSnapshotData(metrics.getMonthlyExamScore(), metrics.getClassAverageScore(), radarJson, null);
            report.setAttitude(95.0, metrics.getBooksReadCount(), metrics.getOvercomeWrongCount(), metrics.getTopWeakTypeTag());
        }
        return repository.save(report);
    }

    /**
     * 학생의 과거 리포트 내역을 전체 조회하여 차트 렌더링용 Map 구조로 변환
     */
    @Transactional(readOnly = true)
    public Map<String, List<?>> getScoreTrend(Long studentId) {
        List<ParentReport> pastReports = repository.findAllByStudentIdOrderByPeriodEndAsc(studentId);

        List<String> labels = new ArrayList<>();
        List<Double> studentScores = new ArrayList<>();
        List<Double> classScores = new ArrayList<>();

        for (ParentReport r : pastReports) {
            labels.add(r.getPeriodEnd().getMonthValue() + "월");
            studentScores.add(r.getMonthlyExamScore());
            classScores.add(r.getClassAverageScore());
        }

        Map<String, List<?>> trendData = new HashMap<>();
        trendData.put("chartLabels", labels);
        trendData.put("studentScores", studentScores);
        trendData.put("classScores", classScores);

        return trendData;
    }
}