package com.example.EduPOP.controller.exp;

import com.example.EduPOP.domain.exp.ExpLog;
import com.example.EduPOP.domain.user.User;
import com.example.EduPOP.domain.user.UserRole;
import com.example.EduPOP.domain.user.UserStatus;
import com.example.EduPOP.dto.exp.ExpDTO;
import com.example.EduPOP.service.exp.ExpService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exp") // 경험치 조회 주소의 공통 시작 경로
public class ExpController {

    private final ExpService expService;
    // 서비스와 연결


    @GetMapping("/me") // 로그인한 학생의 현재 경험치 정보 조회
    public ExpDTO getMyExp(
            HttpSession session
    ) { // : 내 경험치와 캐릭터 정보 반환

        User loginStudent =
                getLoginStudent(
                        session
                );

        return expService.getExpInfo(
                loginStudent.getUserId()
        ); // 로그인한 학생의 경험치 정보를 반환
    }


    @GetMapping("/me/logs") // 로그인한 학생의 경험치 지급 로그 조회
    public List<ExpLog> getMyExpLogs(
            HttpSession session
    ) { // 내 경험치 지급 로그 목록 반환

        User loginStudent =
                getLoginStudent(
                        session
                );

        return expService.getExpLogs(
                loginStudent.getUserId()
        ); // 로그인한 학생의 경험치 지급 로그 목록 반환
    }


    private User getLoginStudent(
            HttpSession session
    ) { // : 로그인한 학생인지 확인

        Object loginUser =
                session.getAttribute(
                        "loginUser"
                ); // 세션에서 로그인한 사용자 객체 조회

        if (!(loginUser instanceof User user)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "로그인이 필요합니다."
            ); // 로그인하지 않았다면 401 상태 반환
        }

        if (user.getRole() != UserRole.STUDENT) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "학생 계정만 경험치 정보를 조회할 수 있습니다."
            ); // 학생 계정이 아니라면 403 상태 반환
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "승인된 학생 계정만 경험치 정보를 조회할 수 있습니다."
            ); // 승인되지 않은 학생이라면 403 상태 반환
        }

        return user; // 로그인과 승인이 확인된 학생 객체 반환
    }
}