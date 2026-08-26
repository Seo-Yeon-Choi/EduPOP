package com.example.EduPOP.service.report;

import com.example.EduPOP.domain.report.ParentReport;
import com.example.EduPOP.domain.report.ParentDeviceLink; // 🚀 에러 해결: Import 추가!
import com.example.EduPOP.dto.ReportMetricsDTO;
import com.example.EduPOP.repository.report.ReportMapper;
import com.example.EduPOP.repository.ParentReportRepository;
import com.example.EduPOP.repository.ParentDeviceLinkRepository; // 🚀 에러 해결: Import 추가!
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ParentReportService {

    private final ParentReportRepository repository;
    private final ReportMapper reportMapper;
    // 🚀 에러 해결: 기기 연결 레포지토리 부품 추가!
    private final ParentDeviceLinkRepository deviceLinkRepository;

    // 🚀 에러 해결: 생성자에 기기 연결 레포지토리 연결!
    public ParentReportService(ParentReportRepository repository, ReportMapper reportMapper, ParentDeviceLinkRepository deviceLinkRepository) {
        this.repository = repository;
        this.reportMapper = reportMapper;
        this.deviceLinkRepository = deviceLinkRepository;
    }

    @Transactional(readOnly = true)
    public ParentReport getReport(Long reportId) {
        ParentReport report = repository.findById(reportId).orElse(null);
        if (report != null) {
            String studentName = reportMapper.getUserNameById(report.getStudentId());
            String teacherName = reportMapper.getUserNameById(report.getCreatedBy());
            String className = reportMapper.getClassNameByStudentId(report.getStudentId());
            report.setStudentInfo(studentName != null ? studentName : "알 수 없는 학생", className != null ? className : "소속 반 없음");
            report.setTeacherName(teacherName != null ? teacherName : "담당 선생님 미정");
        }
        return report;
    }

    @Transactional(readOnly = true)
    public ParentReport getReportByToken(String token) {
        ParentReport report = repository.findByAccessToken(token).orElse(null);
        if (report != null) {
            String studentName = reportMapper.getUserNameById(report.getStudentId());
            String teacherName = reportMapper.getUserNameById(report.getCreatedBy());
            String className = reportMapper.getClassNameByStudentId(report.getStudentId());
            report.setStudentInfo(studentName != null ? studentName : "알 수 없는 학생", className != null ? className : "소속 반 없음");
            report.setTeacherName(teacherName != null ? teacherName : "담당 선생님 미정");
        }
        return report;
    }

    @Transactional
    public ParentReport generateReportFromDB(Long studentId, Long teacherId, LocalDate startDate, LocalDate endDate) {
        ReportMetricsDTO metrics = reportMapper.getMonthlyReportMetrics(studentId, teacherId, startDate, endDate);
        String radarJson = "{\"듣기\": 80.0, \"쓰기\": 65.0, \"어휘\": 90.0, \"문법\": 48.0, \"독해\": 92.0}";
        ParentReport report = new ParentReport(studentId, teacherId, startDate, endDate);
        if (metrics != null) {
            report.setStudentInfo(metrics.getStudentName(), metrics.getClassName());
            report.setSnapshotData(metrics.getMonthlyExamScore(), metrics.getClassAverageScore(), radarJson, null);
            report.setAttitude(95.0, metrics.getBooksReadCount(), metrics.getOvercomeWrongCount(), metrics.getTopWeakTypeTag());
        }
        return repository.save(report);
    }

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

    public boolean verifyParentAuth(Long studentId, String inputStudentName, String inputPhoneLast4) {
        String realStudentName = reportMapper.getUserNameById(studentId);
        if (realStudentName == null || !realStudentName.equals(inputStudentName)) return false;

        String realPhone = reportMapper.getParentPhoneByStudentId(studentId);
        if (realPhone == null || realPhone.length() < 4) return false;

        String cleanPhone = realPhone.replaceAll("[^0-9]", "");
        String realLast4 = cleanPhone.substring(cleanPhone.length() - 4);
        return realLast4.equals(inputPhoneLast4);
    }

    @Transactional
    public String registerDeviceLink(Long studentId) {
        ParentDeviceLink deviceLink = new ParentDeviceLink(studentId);
        deviceLinkRepository.save(deviceLink);
        return deviceLink.getDeviceToken();
    }

    @Transactional(readOnly = true)
    public boolean isValidDevice(Long studentId, String deviceToken) {
        if (deviceToken == null) return false;
        ParentDeviceLink link = deviceLinkRepository.findByDeviceToken(deviceToken).orElse(null);
        if (link == null) return false;
        return link.getStudentId().equals(studentId) && link.getExpiresAt().isAfter(LocalDateTime.now());
    }

    @Transactional
    public String publishAndGetShareLink(Long reportId, String baseUrl) {
        ParentReport report = repository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("리포트를 찾을 수 없습니다."));
        report.publish();
        return baseUrl + "/share/reports/" + report.getAccessToken();
    }
}