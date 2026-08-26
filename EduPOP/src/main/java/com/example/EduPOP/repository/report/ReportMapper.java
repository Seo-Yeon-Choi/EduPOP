package com.example.EduPOP.repository.report;

import com.example.EduPOP.dto.ReportMetricsDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDate;

@Mapper
public interface ReportMapper {

    // users 테이블에서 식별자를 통해 이름 조회 (학생, 교사 공용)
    @Select("SELECT name FROM users WHERE user_id = #{userId}")
    String getUserNameById(@Param("userId") Long userId);

    // 학생이 현재 소속된 활성(ACTIVE) 상태의 반 이름 조회
    @Select("SELECT c.name " +
            "FROM classes c " +
            "JOIN class_students cs ON c.class_id = cs.class_id " +
            "WHERE cs.student_id = #{studentId} AND cs.status = 'ACTIVE' LIMIT 1")
    String getClassNameByStudentId(@Param("studentId") Long studentId);

    // 리포트 생성 시점에 학생의 월간 통계 데이터를 집계하여 반환
    ReportMetricsDTO getMonthlyReportMetrics(
            @Param("studentId") Long studentId,
            @Param("teacherId") Long teacherId,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd
    );
    // ========================================================
    // 학생 ID로 학부모 전화번호 가져오기
    // ========================================================
    @Select("SELECT phone FROM users WHERE user_id = #{studentId}")
    String getParentPhoneByStudentId(@Param("studentId") Long studentId);
}