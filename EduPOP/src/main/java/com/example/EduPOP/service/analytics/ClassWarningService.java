package com.example.EduPOP.service.analytics;

import com.example.EduPOP.controller.analytics.dto.ClassWarningResponse;
import com.example.EduPOP.controller.analytics.dto.ClassWarningResponse.ExamComparisonDto;
import com.example.EduPOP.controller.analytics.dto.ClassWarningResponse.StudentVulnerableDto;
import com.example.EduPOP.controller.analytics.dto.StudentTrendResponse.SubCategoryStatDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassWarningService {

    public ClassWarningResponse evaluateClassWarning(
            Long classId,
            String className,
            List<StudentVulnerableDto> students,
            List<ExamComparisonDto> examComparisons,
            List<SubCategoryStatDto> classWorst,
            List<SubCategoryStatDto> classTop
    ) {
        String warningLevel = "GREEN";
        String warningReason = "현재 우리 반은 모든 학습 영역에서 안정적인 성취도를 보이고 있습니다. (특이사항 없음)";

        // 💡 1. 0점이 아닌 유효한 시험 중 가장 최근(또는 점수가 있는) 시험의 반 평균 점수를 안전하게 탐색합니다.
        double latestClassScore = 100.0;
        if (examComparisons != null && !examComparisons.isEmpty()) {
            // 기본적으로 가장 마지막 시험을 보되, 만약 점수가 0점이거나 비어있다면 점수가 존재하는 최근 시험을 탐색
            ExamComparisonDto targetExam = examComparisons.get(examComparisons.size() - 1);

            // 만약 마지막 시험 점수가 0점이고 이전 시험에 점수가 있다면 이전 시험을 타겟으로 지정할 수도 있습니다.
            if (targetExam.getClassAverageScore() != null) {
                latestClassScore = targetExam.getClassAverageScore();
            }
        }

        // 2. 전체 학생 수 대비 오답률이 높은 학생들의 비율(%) 계산
        long totalStudents = (students != null) ? students.size() : 0;
        long highRiskCount = 0;
        long warningCount = 0;

        if (totalStudents > 0) {
            for (StudentVulnerableDto student : students) {
                double errorRate = student.getErrorRate() != null ? student.getErrorRate() : 0.0;
                if (errorRate >= 50.0) {
                    highRiskCount++;
                } else if (errorRate >= 30.0) {
                    warningCount++;
                }
            }
        }

        double highRiskPercent = totalStudents > 0 ? (double) highRiskCount / totalStudents * 100.0 : 0.0;
        double warningPercent = totalStudents > 0 ? (double) warningCount / totalStudents * 100.0 : 0.0;

        // 3. 반 전체 취약 영역 정답률 분석
        boolean hasLowScoreCategory = false;
        SubCategoryStatDto worst = null;

        if (classWorst != null && !classWorst.isEmpty()) {
            worst = classWorst.get(0);
            double scoreRate = worst.getStudentScoreRate() != null ? worst.getStudentScoreRate() : 0.0;
            if (scoreRate < 60.0) {
                hasLowScoreCategory = true;
            }
        }

        // 4. 퍼센트(%) 기반 복합 판정 로직
        if (latestClassScore < 50.0 || highRiskPercent >= 30.0 || (hasLowScoreCategory && worst != null && worst.getStudentScoreRate() < 40.0)) {
            warningLevel = "RED";
            if (latestClassScore < 50.0) {
                warningReason = String.format("🔴 반 우선 보완 신호 : 최근 시험 반 평균 점수가 %.1f점으로 매우 저조합니다. (전체 수강생 중 고위험군 %.1f%%)", latestClassScore, highRiskPercent);
            } else if (highRiskPercent >= 30.0) {
                warningReason = String.format("🔴 반 우선 보완 신호 : 전체 수강생의 %.1f%%(총 %d명)가 고위험 오답군에 속합니다.", highRiskPercent, highRiskCount);
            } else if (worst != null) {
                warningReason = String.format("🔴 반 우선 보완 신호 : '%s - %s' 영역 공통 정답률이 낮고 집중 보완이 시급합니다.",
                        worst.getLargeCategory(), worst.getSmallCategory());
            } else {
                warningReason = "🔴 반 우선 보완 신호 : 오답률이 높은 고위험군 학생들이 다수 포착되었습니다.";
            }
        } else if (latestClassScore < 70.0 || (highRiskPercent + warningPercent) >= 30.0 || hasLowScoreCategory) {
            warningLevel = "YELLOW";
            if (latestClassScore < 70.0) {
                warningReason = String.format("🟡 반 확인 필요 신호 : 최근 시험 반 평균 점수가 %.1f점으로 다소 낮습니다.", latestClassScore);
            } else if ((highRiskPercent + warningPercent) >= 30.0) {
                warningReason = String.format("🟡 반 확인 필요 신호 : 주의/위험군 학생 비율이 %.1f%%에 달합니다.", (highRiskPercent + warningPercent));
            } else if (worst != null) {
                warningReason = String.format("🟡 반 확인 필요 신호 : '%s - %s' 영역(정답률 %.1f%%) 점검이 추천됩니다.",
                        worst.getLargeCategory(), worst.getSmallCategory(), worst.getStudentScoreRate());
            } else {
                warningReason = "🟡 반 확인 필요 신호 : 보완이 필요한 학생이 존재합니다.";
            }
        } else {
            warningLevel = "GREEN";
            warningReason = "🟢 반 안정 성취 : 우리 반 학생들의 학습 성취도와 평균 점수가 매우 안정적입니다.";
        }

        return ClassWarningResponse.builder()
                .classId(classId)
                .className(className)
                .warningLevel(warningLevel)
                .warningReason(warningReason)
                .examComparisons(examComparisons)
                .classWorstCategories(classWorst)
                .classTopCategories(classTop)
                .vulnerableStudents(students)
                .build();
    }
}