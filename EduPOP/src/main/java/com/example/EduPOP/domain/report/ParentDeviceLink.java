package com.example.EduPOP.domain.report;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "parent_device_links")
public class ParentDeviceLink {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long linkId;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false, unique = true, length = 100)
    private String deviceToken;

    @Column(nullable = false)
    private LocalDateTime connectedAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    protected ParentDeviceLink() {}

    public ParentDeviceLink(Long studentId) {
        this.studentId = studentId;
        this.deviceToken = UUID.randomUUID().toString(); // 고유 기기 토큰 생성
        this.connectedAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusYears(1); // 1년 유효
    }

    public Long getStudentId() { return studentId; }
    public String getDeviceToken() { return deviceToken; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
}