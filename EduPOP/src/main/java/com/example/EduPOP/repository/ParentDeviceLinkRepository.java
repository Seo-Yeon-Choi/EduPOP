package com.example.EduPOP.repository;

import com.example.EduPOP.domain.report.ParentDeviceLink;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ParentDeviceLinkRepository extends JpaRepository<ParentDeviceLink, Long> {
    // 🚀 쿠키에 있는 기기 토큰으로 DB에서 등록된 기기를 찾습니다!
    Optional<ParentDeviceLink> findByDeviceToken(String deviceToken);
}