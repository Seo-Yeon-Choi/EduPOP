package com.example.EduPOP.service.analytics;

import com.example.EduPOP.controller.analytics.dto.ClassWarningResponse;
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
            List<ClassWarningResponse.StudentVulnerableDto> students,
            List<ClassWarningResponse.ExamComparisonDto> examComparisons,
            List<SubCategoryStatDto> classWorst,
            List<SubCategoryStatDto> classTop
    ) {
        // 1. [반별 위험신호] 대시보드 상단 위젯용 (오답률 기준 명확화)
        String warningLevel = "GREEN";
        String warningReason = "현재 우리 반은 모든 학습 영역에서 안정적인 성취도를 보이고 있습니다. (특이사항 없음)";

        if (classWorst != null && !classWorst.isEmpty()) {
            SubCategoryStatDto worst = classWorst.get(0);
            double errorRate = worst.getStudentScoreRate() != null ? worst.getStudentScoreRate() : 0.0;

            // 오답률이 높을수록 빨간불(위험)이 뜨도록 명확히 분기
            if (errorRate >= 40.0) {
                warningLevel = "RED";
                warningReason = String.format("반 우선 보완 신호 : '%s - %s' 영역에서 공통 오답률(%.1f%%)이 높아 집중 지도가 필요합니다.",
                        worst.getLargeCategory(), worst.getSmallCategory(), errorRate);
            } else if (errorRate >= 20.0) {
                warningLevel = "YELLOW";
                warningReason = String.format("반 확인 필요 신호 : '%s - %s' 영역(오답률 %.1f%%)은 가벼운 점검이 추천됩니다.",
                        worst.getLargeCategory(), worst.getSmallCategory(), errorRate);
            } else {
                warningLevel = "GREEN";
                warningReason = String.format("반 안정 성취 : '%s - %s' 영역을 우수하게 소화하고 있습니다.",
                        worst.getLargeCategory(), worst.getSmallCategory());
            }
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