package com.example.EduPOP.service.report;

import com.example.EduPOP.controller.analytics.dto.StudentTrendResponse.RadarStatDto;
import com.example.EduPOP.domain.report.ParentDeviceLink;
import com.example.EduPOP.domain.report.ParentReport;
import com.example.EduPOP.dto.ReportMetricsDTO;
import com.example.EduPOP.repository.ParentDeviceLinkRepository;
import com.example.EduPOP.repository.ParentReportRepository;
import com.example.EduPOP.repository.exam.StudentExamMapper;
import com.example.EduPOP.repository.report.ReportMapper;
import com.example.EduPOP.service.analytics.AnalyticsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ParentReportService {

    private final ParentReportRepository repository;

    private final ReportMapper reportMapper;

    private final ParentDeviceLinkRepository deviceLinkRepository;

    // 학생 성적 분석 화면과 같은 영역별 성취도 계산을 재사용
    private final AnalyticsService analyticsService;

    // 학생 리포트와 같은 시험 응시율 계산 메서드를 재사용
    private final StudentExamMapper studentExamMapper;


    public ParentReportService(
            ParentReportRepository repository,
            ReportMapper reportMapper,
            ParentDeviceLinkRepository deviceLinkRepository,
            AnalyticsService analyticsService,
            StudentExamMapper studentExamMapper
    ) {

        this.repository = repository;

        this.reportMapper = reportMapper;

        this.deviceLinkRepository =
                deviceLinkRepository;

        this.analyticsService = analyticsService;

        this.studentExamMapper = studentExamMapper;
    }


    // =========================================================
    // 특정 학부모 리포트 조회
    //
    // 중요:
    //
    // 기존 parent_reports에 저장된 값을 그대로 보여주면
    // 과거 테스트값이 계속 나타날 수 있다.
    //
    // 따라서 조회할 때 실제 원본 데이터를 다시 집계한다.
    //
    // URL은 변경하지 않는다.
    // =========================================================

    @Transactional
    public ParentReport getReport(
            Long reportId
    ) {

        ParentReport report =
                repository.findById(reportId)
                        .orElse(null);


        if (report == null) {
            return null;
        }


        // 최신 실제 데이터로 갱신
        refreshReportData(report);


        // 갱신된 값 저장
        repository.save(report);


        return report;
    }


    // =========================================================
    // 공유 토큰으로 리포트 조회
    //
    // 기존 URL 구조는 그대로 사용한다.
    //
    // 단, 실제 데이터만 최신화한다.
    // =========================================================

    @Transactional
    public ParentReport getReportByToken(
            String token
    ) {

        ParentReport report =
                repository.findByAccessToken(token)
                        .orElse(null);


        if (report == null) {
            return null;
        }


        // 최신 실제 데이터 반영
        refreshReportData(report);


        // 갱신된 값 저장
        repository.save(report);


        return report;
    }


    // =========================================================
    // 학생 이름 / 반 / 교사 이름
    // =========================================================

    private void fillDisplayInfo(
            ParentReport report
    ) {

        String studentName =
                reportMapper.getUserNameById(
                        report.getStudentId()
                );


        String teacherName =
                reportMapper.getUserNameById(
                        report.getCreatedBy()
                );


        String className =
                reportMapper.getClassNameByStudentId(
                        report.getStudentId()
                );


        report.setStudentInfo(
                studentName != null
                        ? studentName
                        : "알 수 없는 학생",

                className != null
                        ? className
                        : "소속 반 없음"
        );


        report.setTeacherName(
                teacherName != null
                        ? teacherName
                        : "담당 선생님 미정"
        );
    }


    // =========================================================
    // ⭐ DB 기반 학부모 리포트 생성
    //
    // 학생의 실제 시험 / 독서 / 오답 데이터를
    // ReportMapper에서 조회한다.
    //
    // 하드코딩 값은 사용하지 않는다.
    // =========================================================

    @Transactional
    public ParentReport generateReportFromDB(
            Long studentId,
            Long teacherId,
            LocalDate startDate,
            LocalDate endDate
    ) {

        // 같은 기간 코멘트 리포트가 먼저 만들어졌다면 새 행을 만들지 않고 재사용
        ParentReport report =
                repository
                        .findTopByStudentIdAndPeriodStartAndPeriodEndOrderByCreatedAtDesc(
                                studentId,
                                startDate,
                                endDate
                        )
                        .orElseGet(() ->
                                new ParentReport(
                                        studentId,
                                        teacherId,
                                        startDate,
                                        endDate
                                )
                        );


        // -----------------------------------------------------
        // 실제 DB 데이터를 리포트에 반영
        // -----------------------------------------------------

        refreshReportData(report);


        // -----------------------------------------------------
        // 최종 저장
        // -----------------------------------------------------

        return repository.save(report);
    }


    // =========================================================
    // ⭐ 기존 리포트의 데이터를 최신 실제 DB 데이터로 갱신
    //
    // 여기가 이번 수정의 핵심.
    //
    // 기존:
    // 95.0
    // 고정 레이더 값
    // 고정 95% 단어 응시율
    //
    // ↓
    //
    // 실제 DB에서 다시 계산
    // =========================================================

    @Transactional
    public ParentReport refreshReportData(
            ParentReport report
    ) {

        if (report == null) {

            throw new IllegalArgumentException(
                    "리포트가 없습니다."
            );
        }


        Long studentId =
                report.getStudentId();

        Long teacherId =
                report.getCreatedBy();

        LocalDate startDate =
                report.getPeriodStart();

        LocalDate endDate =
                report.getPeriodEnd();


        // =====================================================
        // 1. 실제 성과 및 학습 태도 데이터 조회
        // =====================================================

        ReportMetricsDTO metrics =
                reportMapper.getMonthlyReportMetrics(
                        studentId,
                        teacherId,
                        startDate,
                        endDate
                );


        // =====================================================
        // 2. ⭐ 실제 동적 영역별 성취도 조회
        // =====================================================

        List<RadarStatDto> radarStats =
                analyticsService.getRadarStatsByPeriod(
                        studentId,
                        startDate,
                        endDate
                );

        // parent_reports에 저장하는 값도 학생 성적 분석과 같은 조회 결과로 생성
        String radarJson = createRadarJson(radarStats);

        // 학생 리포트와 학부모 리포트가 정확히 같은 시험 응시율을 사용
        Double examCompletionRate =
                studentExamMapper.findExamCompletionRate(
                        studentId,
                        startDate,
                        endDate
                );

        // 학생 리포트와 학부모 리포트가 정확히 같은 극복 문제 수를 사용
        Integer overcomeWrongCount =
                reportMapper.countOvercomeWrongCount(
                        studentId,
                        startDate,
                        endDate
                );


        // =====================================================
        // 3. ⭐ 선생님 코멘트
        //
        // 이 코멘트는 이 서비스가 임의로 생성하는 것이 아니다.
        //
        // 다른 팀원의 시험 채점 코드에서
        //
        // ExamService.saveTeacherComments()
        //        ↓
        // ExamMapper.updateTeacherComment()
        //        ↓
        // parent_reports.teacher_comment
        //
        // 로 저장된 값을 그대로 유지한다.
        // =====================================================

        String teacherComment =
                report.getTeacherComment();


        if (teacherComment == null ||
                teacherComment.isBlank()) {

            teacherComment =
                    "아직 등록된 코멘트가 없습니다.";
        }


        // =====================================================
        // 4. 실제 데이터가 존재하는 경우
        // =====================================================

        if (metrics != null) {

            // -------------------------------------------------
            // 성과 지표
            // -------------------------------------------------

            report.setSnapshotData(
                    safeDouble(
                            metrics.getMonthlyExamScore()
                    ),

                    safeDouble(
                            metrics.getClassAverageScore()
                    ),

                    radarJson,

                    teacherComment
            );


            // -------------------------------------------------
            // 학습 태도 및 달성 지표
            //
            // 하드코딩 금지.
            //
            // 반드시 실제 DB 계산값을 사용한다.
            // -------------------------------------------------

            report.setAttitude(

                    safeDouble(examCompletionRate),

                    metrics.getBooksReadCount() != null
                            ? metrics.getBooksReadCount()
                            : 0,

                    overcomeWrongCount != null
                            ? overcomeWrongCount
                            : 0,

                    metrics.getTopWeakTypeTag() != null
                            ? metrics.getTopWeakTypeTag()
                            : "분석 중"
            );

        } else {

            // =================================================
            // 통계 데이터가 없는 경우
            // =================================================

            report.setSnapshotData(
                    0.0,
                    0.0,
                    radarJson,
                    teacherComment
            );


            report.setAttitude(
                    0.0,
                    0,
                    0,
                    "분석 중"
            );
        }


        // =====================================================
        // 5. 학생 / 반 / 교사 정보 갱신
        // =====================================================

        fillDisplayInfo(report);


        return report;
    }


    // =========================================================
    // 학부모 화면용 영역별 성취도
    //
    // 학생 성적 분석 화면과 동일한 AnalyticsService 메서드를 호출한다.
    // 상세 URL과 공유 URL이 모두 이 메서드를 사용한다.
    // =========================================================

    @Transactional(readOnly = true)
    public List<RadarStatDto> getRadarStats(
            ParentReport report
    ) {

        if (report == null) {
            return new ArrayList<>();
        }

        List<RadarStatDto> radarStats =
                analyticsService.getRadarStatsByPeriod(
                        report.getStudentId(),
                        report.getPeriodStart(),
                        report.getPeriodEnd()
                );

        return radarStats != null
                ? radarStats
                : new ArrayList<>();
    }


    // =========================================================
    // ⭐ 동적 레이더 차트 JSON 생성
    //
    // 절대로:
    //
    // 듣기
    // 쓰기
    // 어휘
    // 문법
    // 독해
    //
    // 를 코드에 고정하지 않는다.
    //
    // student-trend와 동일하게 exam_questions.large_category를 사용한다.
    // =========================================================

    private String createRadarJson(
            List<RadarStatDto> radarStats
    ) {


        Map<String, Double> radarData =
                new LinkedHashMap<>();


        if (radarStats == null ||
                radarStats.isEmpty()) {

            return "{}";
        }


        for (RadarStatDto stat : radarStats) {

            if (stat == null || stat.getTag() == null) {
                continue;
            }

            String category =
                    stat.getTag();

            double score =
                    stat.getStudentScoreRate() != null
                            ? stat.getStudentScoreRate()
                            : 0.0;


            // -------------------------------------------------
            // 같은 영역명이 중복될 경우
            // 마지막 값을 사용한다.
            // -------------------------------------------------

            radarData.put(
                    category,
                    score
            );
        }


        if (radarData.isEmpty()) {
            return "{}";
        }


        // -----------------------------------------------------
        // JSON 문자열 직접 생성
        // -----------------------------------------------------

        StringBuilder json =
                new StringBuilder("{");


        int index = 0;


        for (Map.Entry<String, Double> entry :
                radarData.entrySet()) {

            if (index > 0) {
                json.append(",");
            }


            json.append("\"")
                    .append(
                            escapeJson(
                                    entry.getKey()
                            )
                    )
                    .append("\":")
                    .append(
                            entry.getValue()
                    );


            index++;
        }


        json.append("}");


        return json.toString();
    }


    // =========================================================
    // JSON 문자열 특수문자 처리
    // =========================================================

    private String escapeJson(
            String value
    ) {

        if (value == null) {
            return "";
        }


        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }


    // =========================================================
    // 월말평가 점수 추이
    //
    // 실제 exams + exam_attempts를 조회한다.
    //
    // parent_reports의 과거 스냅샷을 사용하지 않는다.
    // =========================================================

    @Transactional(readOnly = true)
    public Map<String, List<?>> getScoreTrend(
            Long studentId
    ) {

        // -----------------------------------------------------
        // 최초 월말평가부터 현재까지 누적 데이터 전체 조회
        // 화면에서 12개월씩 나눠서 페이징한다.
        // -----------------------------------------------------

        List<Map<String, Object>> rows =
                reportMapper.findMonthlyScoreTrend(
                        studentId
                );


        List<String> labels =
                new ArrayList<>();


        List<Double> studentScores =
                new ArrayList<>();


        List<Double> classScores =
                new ArrayList<>();


        if (rows != null) {

            for (Map<String, Object> row :
                    rows) {

                Object monthObject =
                        row.get("monthLabel");

                Object studentObject =
                        row.get("studentScore");

                Object classObject =
                        row.get("classScore");


                // -------------------------------------------------
                // 월 표시
                // -------------------------------------------------

                if (monthObject != null) {

                    String month =
                            monthObject.toString();


                    if (month.contains("-")) {

                        String[] parts =
                                month.split("-");


                        labels.add(
                                parts[0]
                                        + "년 "
                                        + Integer.parseInt(parts[1])
                                        + "월"
                        );

                    } else {

                        labels.add(month);
                    }
                }


                // -------------------------------------------------
                // 학생 점수
                // -------------------------------------------------

                studentScores.add(
                        safeDouble(
                                studentObject
                        )
                );


                // -------------------------------------------------
                // 반 평균
                // -------------------------------------------------

                classScores.add(
                        safeDouble(
                                classObject
                        )
                );
            }
        }


        Map<String, List<?>> result =
                new LinkedHashMap<>();


        result.put(
                "chartLabels",
                labels
        );


        result.put(
                "studentScores",
                studentScores
        );


        result.put(
                "classScores",
                classScores
        );


        return result;
    }


    // =========================================================
    // 숫자 안전 변환 - Double 객체
    // =========================================================

    private Double safeDouble(
            Double value
    ) {

        return value != null
                ? value
                : 0.0;
    }


    // =========================================================
    // 숫자 안전 변환 - Object
    // =========================================================

    private Double safeDouble(
            Object value
    ) {

        if (value instanceof Number) {

            return ((Number) value)
                    .doubleValue();
        }

        return 0.0;
    }


    // =========================================================
    // 학부모 인증
    //
    // 기존 URL/인증 방식은 그대로 유지한다.
    // =========================================================

    @Transactional(readOnly = true)
    public boolean verifyParentAuth(
            Long studentId,
            String inputStudentName,
            String inputPhoneLast4
    ) {

        String realStudentName =
                reportMapper.getUserNameById(
                        studentId
                );


        if (realStudentName == null ||
                inputStudentName == null) {

            return false;
        }


        if (!realStudentName
                .trim()
                .equals(
                        inputStudentName.trim()
                )) {

            return false;
        }


        String realPhone =
                reportMapper.getParentPhoneByStudentId(
                        studentId
                );


        if (realPhone == null) {
            return false;
        }


        String cleanPhone =
                realPhone.replaceAll(
                        "[^0-9]",
                        ""
                );


        String cleanInput =
                inputPhoneLast4 == null
                        ? ""
                        : inputPhoneLast4
                        .replaceAll(
                                "[^0-9]",
                                ""
                        );


        if (cleanPhone.length() < 4 ||
                cleanInput.length() != 4) {

            return false;
        }


        String realLast4 =
                cleanPhone.substring(
                        cleanPhone.length() - 4
                );


        return realLast4.equals(
                cleanInput
        );
    }


    // =========================================================
    // 학부모 기기 연결
    //
    // URL 관련 코드 유지
    // =========================================================

    @Transactional
    public String registerDeviceLink(
            Long studentId
    ) {

        ParentDeviceLink deviceLink =
                new ParentDeviceLink(
                        studentId
                );


        deviceLinkRepository.save(
                deviceLink
        );


        return deviceLink.getDeviceToken();
    }


    // =========================================================
    // 기기 연결 확인
    // =========================================================

    @Transactional(readOnly = true)
    public boolean isValidDevice(
            Long studentId,
            String deviceToken
    ) {

        if (deviceToken == null) {
            return false;
        }


        ParentDeviceLink link =
                deviceLinkRepository
                        .findByDeviceToken(
                                deviceToken
                        )
                        .orElse(null);


        if (link == null) {
            return false;
        }


        return link.getStudentId().equals(
                studentId
        )
                &&
                link.getExpiresAt()
                        .isAfter(
                                LocalDateTime.now()
                        );
    }


    // =========================================================
    // 공유 링크 발행
    //
    // URL 생성 부분은 기존과 동일하다.
    //
    // 단, 링크를 발행하기 직전에
    // 리포트 데이터를 실제 DB 값으로 갱신한다.
    // =========================================================

    @Transactional
    public String publishAndGetShareLink(
            Long reportId,
            String baseUrl
    ) {

        ParentReport report =
                repository.findById(reportId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "리포트를 찾을 수 없습니다."
                                )
                        );


        // 최신 데이터 반영
        refreshReportData(report);


        // 기존 발행 방식 유지
        report.publish();


        // 기존 URL 형식 유지
        return baseUrl
                + "/share/reports/"
                + report.getAccessToken();
    }


    // =========================================================
    // 단톡방/반 링크 인증
    //
    // URL 로직은 그대로 유지한다.
    // =========================================================

    @Transactional(readOnly = true)
    public String authenticateAndGetReportToken(
            Long classId,
            String studentName,
            String phoneLast4
    ) {

        Long studentId =
                reportMapper.findStudentIdByClassAndName(
                        classId,
                        studentName
                );


        if (studentId == null) {
            return null;
        }


        boolean isAuthValid =
                verifyParentAuth(
                        studentId,
                        studentName,
                        phoneLast4
                );


        if (!isAuthValid) {
            return null;
        }


        return reportMapper.findLatestPublishedReportToken(
                studentId
        );
    }
}
