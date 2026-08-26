package com.example.EduPOP.dto;

import java.util.Map;

public class RadarScoreDTO {
    private Long studentId;

    // 과목 수가 유동적이므로 Key(과목명)-Value(점수) 형태의 Map 구조 사용
    private Map<String, Double> radarScores;
    private String topWeakTag;

    // === Getters and Setters ===
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Map<String, Double> getRadarScores() { return radarScores; }
    public void setRadarScores(Map<String, Double> radarScores) { this.radarScores = radarScores; }

    public String getTopWeakTag() { return topWeakTag; }
    public void setTopWeakTag(String topWeakTag) { this.topWeakTag = topWeakTag; }
}