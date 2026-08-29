package com.example.EduPOP.service.exp;

import com.example.EduPOP.domain.exp.ExpActivityType;
import com.example.EduPOP.domain.exp.ExpGrowth;
import com.example.EduPOP.domain.exp.ExpLevel;
import com.example.EduPOP.domain.exp.ExpLog;
import com.example.EduPOP.dto.exp.ExpDTO;
import com.example.EduPOP.repository.exp.ExpMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal; // BigDecimal(빅 데시멀): 소수점 계산용 숫자 타입
import java.math.RoundingMode; // RoundingMode(라운딩 모드): 소수점 처리 방법
import java.time.LocalDate; // LocalDate(로컬 데이트): 오늘의 복습 날짜
import java.time.format.DateTimeFormatter; // DateTimeFormatter(데이트 타임 포매터): 날짜 형식 변환
import java.util.List; // List(리스트): 여러 경험치 로그를 저장하는 자료형

@Service // 경험치 지급과 캐릭터 성장 판단을 담당하는 객체로 등록
@RequiredArgsConstructor // final 필드를 받는 생성자를 자동 생성
@Transactional(readOnly = true) // 조회 작업에서는 DB 내용을 변경하지 않도록 설정
public class ExpService {

    private final ExpMapper expMapper; // 경험치 관련 DB 작업을 Mapper에 요청하기 위해 주입


    public ExpDTO getExpInfo(
            Long studentId
    ) { // getExpInfo(겟 이엑스피 인포): 학생의 메인 화면용 경험치 정보 조회

        validateId(studentId, "학생 번호"); // 올바른 학생 번호인지 확인

        ExpGrowth expGrowth =
                expMapper.findExpGrowthByStudentId(
                        studentId
                ); // 학생의 누적 경험치와 캐릭터 단계 조회

        int totalExp =
                expGrowth == null || expGrowth.getTotalExp() == null
                        ? 0
                        : expGrowth.getTotalExp();
        // 성장 기록이 없는 학생은 총 경험치를 0점으로 사용

        ExpLevel currentLevel =
                ExpLevel.findLevel(
                        totalExp
                );
        // currentLevel(커런트 레벨): 총 경험치에 맞는 현재 성장 단계 조회

        int characterStage =
                currentLevel.getCharacterStage();
        // characterStage(캐릭터 스테이지): 현재 성장 단계 번호

        ExpDTO expDto =
                new ExpDTO();
        // ExpDTO(이엑스피 디티오): 웹페이지에 전달할 경험치 정보 객체 생성

        expDto.setStudentId(
                studentId
        ); // 경험치 정보를 조회한 학생 번호 저장

        expDto.setTotalExp(
                totalExp
        ); // 현재 총 경험치 저장

        expDto.setCharacterStage(
                characterStage
        ); // 현재 캐릭터 성장 단계 저장

        expDto.setStageName(
                currentLevel.getStageName()
        ); // G부터 GROW UP까지 현재 단계 이름 저장

        expDto.setExpToNextStage(
                calculateExpToNextStage(
                        totalExp,
                        currentLevel
                )
        ); // 다음 캐릭터 단계까지 필요한 경험치 저장

        expDto.setExpProgressPercent(
                calculateExpProgressPercent(
                        totalExp,
                        currentLevel
                )
        ); // 현재 단계의 경험치 진행률 저장

        expDto.setMaxStage(
                currentLevel == ExpLevel.LEVEL_6
        ); // 현재 캐릭터가 최종 6단계인지 저장

        expDto.setCharacterImageUrl(
                findCharacterImageUrl()
        ); // 공통 캐릭터 이미지 주소 저장

        expDto.setBackgroundImageUrl(
                findBackgroundImageUrl(
                        characterStage
                )
        ); // 현재 단계에 맞는 배경 이미지 주소 저장

        return expDto; // 완성한 경험치 정보를 Controller에 반환
    }

    @Transactional // 시험 제출 종류에 맞는 경험치 지급 작업을 하나의 작업 단위로 처리
    public int giveAttemptExp(
            Long studentId,
            Long examId,
            String attemptType,
            BigDecimal totalScore,
            BigDecimal maxScore
    ) { // giveAttemptExp(기브 어템프트 이엑스피): 시험 응시 종류에 맞는 경험치 지급

        if ("EXAM".equals(attemptType)) {
            return giveExamExp(
                    studentId,
                    examId,
                    totalScore,
                    maxScore
            ); // 일반 시험이면 점수에 맞는 시험 경험치 지급
        }

        if ("REVIEW".equals(attemptType)) {
            return giveReviewExp(
                    studentId,
                    examId
            ); // 복습 시험이면 같은 원본 시험당 최초 한 번 경험치 30점 지급
        }

        throw new IllegalArgumentException(
                "올바르지 않은 시험 응시 종류입니다."
        ); // EXAM 또는 REVIEW가 아니면 잘못된 요청으로 처리
    }

    @Transactional // 시험 경험치 지급 작업을 하나의 작업 단위로 처리
    public int giveExamExp(
            Long studentId,
            Long examId,
            BigDecimal totalScore,
            BigDecimal maxScore
    ) { // giveExamExp(기브 이그잼 이엑스피): 일반 시험 경험치 지급

        validateId(
                studentId,
                "학생 번호"
        ); // 올바른 학생 번호인지 확인

        validateId(
                examId,
                "시험 번호"
        ); // 올바른 시험 번호인지 확인

        BigDecimal convertedScore =
                calculateConvertedScore(
                        totalScore,
                        maxScore
                );
        // convertedScore(컨버티드 스코어): 100점 만점으로 환산한 시험 점수

        int earnedExp =
                findExamExpAmount(
                        convertedScore
                );
        // earnedExp(언드 이엑스피): 시험 점수에 따라 지급할 경험치

        return giveExp(
                studentId,
                ExpActivityType.EXAM_LOG,
                examId,
                earnedExp
        ); // 같은 시험 최초 1회에만 시험 경험치 지급
    }


    @Transactional // 복습 경험치 지급 작업을 하나의 작업 단위로 처리
    public int giveReviewExp(
            Long studentId,
            Long examId
    ) { // giveReviewExp(기브 리뷰 이엑스피): 시험 복습 경험치 지급

        validateId(
                studentId,
                "학생 번호"
        ); // 올바른 학생 번호인지 확인

        validateId(
                examId,
                "복습한 시험 번호"
        ); // 올바른 원본 시험 번호인지 확인

        return giveExp(
                studentId,
                ExpActivityType.REVIEW_LOG,
                examId,
                30
        ); // 같은 시험 복습은 최초 1회에만 경험치 30점 지급
    }
    @Transactional // 오늘의 복습 경험치 지급 작업을 하나의 작업 단위로 처리
    public int giveDailyReviewExp(
            Long studentId,
            LocalDate reviewDate,
            int correctCount,
            int submittedAnswerCount,
            int totalQuestionCount
    ) { // giveDailyReviewExp(기브 데일리 리뷰 이엑스피): 오늘의 복습 경험치 지급

        validateId(
                studentId,
                "학생 번호"
        ); // 올바른 학생 번호인지 확인

        if (reviewDate == null) {
            throw new IllegalArgumentException(
                    "오늘의 복습 날짜가 필요합니다."
            );
        }

        if (totalQuestionCount <= 0) {
            return 0;
            // 오늘 복습할 문제가 없으면 경험치를 지급하지 않음
        }

        if (submittedAnswerCount != totalQuestionCount) {
            return 0;
            // 제출 답안 수가 전체 문제 수와 다르면 완료로 인정하지 않음
        }

        if (correctCount < 0
                || correctCount > totalQuestionCount) {

            throw new IllegalArgumentException(
                    "오늘의 복습 정답 개수가 올바르지 않습니다."
            );
        }

        if ((long) correctCount * 100
                < (long) totalQuestionCount * 80) {

            return 0;
            // 정답률이 80% 미만이면 경험치를 지급하지 않음
        }

        Long reviewDateReferenceId =
                Long.valueOf(
                        reviewDate.format(
                                DateTimeFormatter.BASIC_ISO_DATE
                        )
                );
        // 오늘 날짜를 20260829 형태의 중복 확인 번호로 변환

        return giveExp(
                studentId,
                ExpActivityType.DAILY_REVIEW_LOG,
                reviewDateReferenceId,
                20
        );
        // 같은 날짜의 오늘의 복습 경험치는 최초 한 번만 20점 지급
    }

    @Transactional // 독서 경험치 지급 작업을 하나의 작업 단위로 처리
    public int giveReadingExp(
            Long studentId,
            Long readingReportId,
            int readingCount
    ) { // giveReadingExp(기브 리딩 이엑스피): 독서감상문 경험치 지급

        validateId(
                studentId,
                "학생 번호"
        ); // 올바른 학생 번호인지 확인

        validateId(
                readingReportId,
                "독서감상문 번호"
        ); // 올바른 감상문 번호인지 확인

        if (readingCount <= 0) {
            throw new IllegalArgumentException(
                    "같은 책으로 작성한 감상문 순서는 1 이상이어야 합니다."
            );
        }

        int earnedExp =
                findReadingExpAmount(
                        readingCount
                );
        // earnedExp(언드 이엑스피): 감상문 작성 순서에 따라 지급할 경험치

        if (earnedExp == 0) {
            return 0; // 같은 책의 다섯 번째 감상문부터는 경험치를 지급하지 않음
        }

        return giveExp(
                studentId,
                ExpActivityType.READING_LOG,
                readingReportId,
                earnedExp
        ); // 감상문 번호를 기준으로 중복 지급 방지
    }


    public List<ExpLog> getExpLogs(
            Long studentId
    ) { // getExpLogs(겟 이엑스피 로그스): 학생의 경험치 지급 로그 목록 조회

        validateId(
                studentId,
                "학생 번호"
        ); // 올바른 학생 번호인지 확인

        return expMapper.findExpLogsByStudentId(
                studentId
        ); // 학생의 경험치 지급 로그 목록 반환
    }


    private int giveExp(
            Long studentId,
            ExpActivityType expLogType,
            Long referenceId,
            int earnedExp
    ) { // giveExp(기브 이엑스피): 공통 경험치 지급 처리

        int duplicateCount =
                expMapper.countDuplicateExp(
                        studentId,
                        expLogType.name(),
                        referenceId
                );
        // duplicateCount(듀플리케이트 카운트): 이미 지급된 같은 활동 개수

        if (duplicateCount > 0) {
            return 0; // 이미 지급된 활동이면 경험치를 다시 지급하지 않음
        }

        ExpGrowth expGrowth =
                expMapper.findExpGrowthByStudentId(
                        studentId
                ); // 학생의 기존 성장 정보 조회

        if (expGrowth == null) {
            insertFirstExpGrowth(
                    studentId,
                    earnedExp
            ); // 처음 경험치를 받는 학생의 성장 정보 등록

        } else {
            addExpToGrowth(
                    expGrowth,
                    earnedExp
            ); // 기존 성장 정보에 새 경험치 추가
        }

        ExpLog expLog =
                new ExpLog(); // DB에 저장할 경험치 지급 로그 객체 생성

        expLog.setStudentId(
                studentId
        ); // 경험치를 받은 학생 번호 저장

        expLog.setActivityType(
                expLogType.name()
        ); // 경험치를 받은 활동 로그 종류 저장

        expLog.setReferenceId(
                referenceId
        ); // 경험치를 발생시킨 시험·복습·감상문 번호 저장

        expLog.setEarnedExp(
                earnedExp
        ); // 실제 지급한 경험치 저장

        int insertedLogCount =
                expMapper.insertExpLog(
                        expLog
                );
        // insertedLogCount(인서티드 로그 카운트): 등록된 로그 개수

        if (insertedLogCount != 1
                || expLog.getLogId() == null) {

            throw new IllegalStateException(
                    "경험치 지급 로그 등록에 실패했습니다."
            );
        }

        return earnedExp; // 실제로 지급한 경험치 반환
    }


    private void insertFirstExpGrowth(
            Long studentId,
            int earnedExp
    ) { // insertFirstExpGrowth(인서트 퍼스트 이엑스피 그로스): 첫 성장 정보 등록

        ExpGrowth newExpGrowth =
                new ExpGrowth();
        // newExpGrowth(뉴 이엑스피 그로스): 처음 저장할 성장 객체

        newExpGrowth.setStudentId(
                studentId
        ); // 경험치를 받은 학생 번호 저장

        newExpGrowth.setTotalExp(
                earnedExp
        ); // 처음 지급한 경험치를 총 경험치로 저장

        newExpGrowth.setCharacterStage(
                ExpLevel.findCharacterStage(
                        earnedExp
                )
        ); // 처음 지급한 경험치에 맞는 캐릭터 단계 저장

        int insertedGrowthCount =
                expMapper.insertExpGrowth(
                        newExpGrowth
                );
        // insertedGrowthCount(인서티드 그로스 카운트): 등록된 성장 정보 개수

        if (insertedGrowthCount != 1) {
            throw new IllegalStateException(
                    "학생 성장 정보 등록에 실패했습니다."
            );
        }
    }


    private void addExpToGrowth(
            ExpGrowth expGrowth,
            int earnedExp
    ) { // addExpToGrowth(애드 이엑스피 투 그로스): 기존 성장 정보에 경험치 추가

        int updatedExpCount =
                expMapper.addExp(
                        expGrowth.getStudentId(),
                        earnedExp
                );
        // updatedExpCount(업데이트드 이엑스피 카운트): 경험치가 수정된 행 개수

        if (updatedExpCount != 1) {
            throw new IllegalStateException(
                    "경험치 지급에 실패했습니다."
            );
        }

        int savedTotalExp =
                expGrowth.getTotalExp() == null
                        ? 0
                        : expGrowth.getTotalExp();
        // savedTotalExp(세이브드 토탈 이엑스피): DB에 저장돼 있던 총 경험치

        int newTotalExp =
                savedTotalExp + earnedExp;
        // newTotalExp(뉴 토탈 이엑스피): 경험치 지급 후 새로운 총 경험치

        int newCharacterStage =
                ExpLevel.findCharacterStage(
                        newTotalExp
                );
        // newCharacterStage(뉴 캐릭터 스테이지): 지급 후 캐릭터 단계

        if (expGrowth.getCharacterStage() != null
                && expGrowth.getCharacterStage()
                == newCharacterStage) {

            return; // 캐릭터 단계가 그대로라면 단계 수정 생략
        }

        int updatedStageCount =
                expMapper.updateExpStage(
                        expGrowth.getStudentId(),
                        newCharacterStage
                );
        // updatedStageCount(업데이트드 스테이지 카운트): 단계가 수정된 행 개수

        if (updatedStageCount != 1) {
            throw new IllegalStateException(
                    "캐릭터 단계 변경에 실패했습니다."
            );
        }
    }


    private BigDecimal calculateConvertedScore(
            BigDecimal totalScore,
            BigDecimal maxScore
    ) { // calculateConvertedScore(캘큘레이트 컨버티드 스코어): 100점 만점 점수 계산

        if (totalScore == null || maxScore == null) {
            throw new IllegalArgumentException(
                    "시험 점수와 시험 만점이 필요합니다."
            );
        }

        if (totalScore.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            throw new IllegalArgumentException(
                    "시험 점수는 0점보다 작을 수 없습니다."
            );
        }

        if (maxScore.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            throw new IllegalArgumentException(
                    "시험 만점은 0점보다 커야 합니다."
            );
        }

        if (totalScore.compareTo(maxScore) > 0) {
            throw new IllegalArgumentException(
                    "시험 점수는 시험 만점보다 클 수 없습니다."
            );
        }

        return totalScore
                .multiply(
                        BigDecimal.valueOf(100)
                )
                .divide(
                        maxScore,
                        2,
                        RoundingMode.HALF_UP
                ); // HALF_UP(하프 업): 소수점 셋째 자리에서 반올림
    }


    private int findExamExpAmount(
            BigDecimal convertedScore
    ) { // findExamExpAmount(파인드 이그잼 이엑스피 어마운트): 시험 점수에 맞는 경험치 선택

        if (convertedScore.compareTo(
                BigDecimal.valueOf(100)
        ) >= 0) {

            return 50; // 환산 시험 점수가 100점이면 경험치 50점
        }

        if (convertedScore.compareTo(
                BigDecimal.valueOf(80)
        ) >= 0) {

            return 40; // 환산 시험 점수가 80점 이상이면 경험치 40점
        }

        if (convertedScore.compareTo(
                BigDecimal.valueOf(70)
        ) >= 0) {

            return 30; // 환산 시험 점수가 70점 이상이면 경험치 30점
        }

        return 20; // 환산 시험 점수가 70점 미만이면 경험치 20점
    }


    private int findReadingExpAmount(
            int readingCount
    ) { // findReadingExpAmount(파인드 리딩 이엑스피 어마운트): 감상문 순서에 맞는 경험치 선택

        return switch (readingCount) {

            case 1 -> 35; // 같은 책의 첫 번째 감상문이면 경험치 35점

            case 2 -> 25; // 같은 책의 두 번째 감상문이면 경험치 25점

            case 3 -> 15; // 같은 책의 세 번째 감상문이면 경험치 15점

            case 4 -> 5; // 같은 책의 네 번째 감상문이면 경험치 5점

            default -> 0; // 같은 책의 다섯 번째 이상이면 경험치를 지급하지 않음
        };
    }


    private int calculateExpToNextStage(
            int totalExp,
            ExpLevel currentLevel
    ) { // calculateExpToNextStage(캘큘레이트 이엑스피 투 넥스트 스테이지): 다음 단계까지 남은 경험치 계산

        ExpLevel nextLevel =
                currentLevel.findNextLevel();
        // nextLevel(넥스트 레벨): 현재 단계 다음의 성장 단계

        if (nextLevel == null) {
            return 0; // 최종 6단계이면 필요한 다음 단계 경험치는 0점
        }

        return Math.max(
                nextLevel.getMinExp() - totalExp,
                0
        ); // 다음 성장 단계까지 필요한 경험치 계산
    }


    private int calculateExpProgressPercent(
            int totalExp,
            ExpLevel currentLevel
    ) { // calculateExpProgressPercent(캘큘레이트 이엑스피 프로그레스 퍼센트): 현재 단계 진행률 계산

        ExpLevel nextLevel =
                currentLevel.findNextLevel();
        // 현재 단계 다음의 성장 단계 조회

        if (nextLevel == null) {
            return 100; // 최종 6단계이면 진행률을 100퍼센트로 반환
        }

        int currentStageExp =
                Math.max(
                        totalExp - currentLevel.getMinExp(),
                        0
                );
        // currentStageExp(커런트 스테이지 이엑스피): 현재 단계에서 획득한 경험치

        int currentStageMaxExp =
                nextLevel.getMinExp()
                        - currentLevel.getMinExp();
        // currentStageMaxExp(커런트 스테이지 맥스 이엑스피): 현재 단계의 전체 경험치 구간

        int progressPercent =
                currentStageExp * 100
                        / currentStageMaxExp;
        // progressPercent(프로그레스 퍼센트): 현재 단계 경험치 진행률

        return Math.min(
                progressPercent,
                100
        ); // 진행률이 100퍼센트를 넘지 않도록 제한
    }


    private String findCharacterImageUrl() {
        // findCharacterImageUrl(파인드 캐릭터 이미지 유알엘): 공통 캐릭터 이미지 주소 조회

        return "/images/exp/character.png"; // 모든 단계에서 사용하는 캐릭터 이미지 주소
    }


    private String findBackgroundImageUrl(
            int characterStage
    ) { // findBackgroundImageUrl(파인드 백그라운드 이미지 유알엘): 단계별 배경 주소 조회

        return "/images/exp/stage"
                + characterStage
                + "-background.png";
        // 현재 단계 번호에 맞는 배경 이미지 주소 반환
    }


    private void validateId(
            Long id,
            String fieldName
    ) { // validateId(밸리데이트 아이디): 전달받은 번호가 올바른지 검사

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    fieldName + "가 올바르지 않습니다."
            );
        }
    }
}