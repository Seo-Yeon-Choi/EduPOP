package com.example.EduPOP.component;

import com.example.EduPOP.repository.user.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor

public class scheduler {
    private final UserMapper userMapper;

    @Scheduled(cron = "0 0 4 * * ?")
    // cron = "초 분 시 일 월 요일" 순서
    // "0 0 4 * * ?" 뜻 = "매일 새벽 4시 0분 0초에 실행해라"
    public void cleanUpWithdrawnUsers() {
        System.out.println("🧹 [스케줄러 실행] 1년이 지난 탈퇴 회원을 정리합니다...");
        userMapper.deleteOldWithdrawnUsers();
        System.out.println("✨ [스케줄러 완료] 정리가 완료되었습니다.");
    }
}



