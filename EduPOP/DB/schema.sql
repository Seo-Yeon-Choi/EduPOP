-- =========================================================================================
-- [도메인 1] 학원 및 사용자 / 역할 / 권한 기반 도메인 (academy_modules 제거 완료)
-- =========================================================================================
CREATE DATABASE edupop;

CREATE TABLE academies (
                           academy_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           name VARCHAR(100) NOT NULL,                 -- 학원 명칭
                           address VARCHAR(255),                       -- 학원 주소
                           phone VARCHAR(30),                          -- 학원 전화번호
                           business_cer VARCHAR(100) NOT NULL,         -- 사업자등록증 첨부 이미지 URL
                           created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE users (
                       user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       academy_id BIGINT,                          -- 소속 학원 (학부모 등 소속 학원이 없는 경우 NULL 가능)
                       login_id VARCHAR(100) NOT NULL,             -- 로그인 아이디
                       password_hash VARCHAR(255) NOT NULL,        -- 암호화된 비밀번호
                       name VARCHAR(50) NOT NULL,                  -- 사용자 실명
                       email VARCHAR(150),                         -- 이메일 주소
                       phone VARCHAR(30),                          -- 전화번호
                       role VARCHAR(20) NOT NULL,                  -- ADMIN, TEACHER, STUDENT, PARENT
                       status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, ACTIVE, INACTIVE, WITHDRAWN
                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       withdrawn_at DATETIME,                      -- 탈퇴 시점 기록 (탈퇴 후 1년 보관 기간 측정용)

                       CONSTRAINT uk_users_academy_login UNIQUE (academy_id, login_id),
                       CONSTRAINT fk_users_academy FOREIGN KEY (academy_id) REFERENCES academies(academy_id)
);

CREATE TABLE parent_students (
                                 parent_student_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 parent_id BIGINT NOT NULL,                  -- 학부모 (users.role = 'PARENT')
                                 student_id BIGINT NOT NULL,                 -- 자녀 (users.role = 'STUDENT')
                                 created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT uk_parent_students UNIQUE (parent_id, student_id),
                                 CONSTRAINT fk_ps_parent FOREIGN KEY (parent_id) REFERENCES users(user_id) ON DELETE CASCADE,
                                 CONSTRAINT fk_ps_student FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE
);


-- =========================================================================================
-- [도메인 2] 반 관리 & 다중 강사/수강생 매핑 (담당 기능: 1.2.1)
-- =========================================================================================

CREATE TABLE classes (
                         class_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         academy_id BIGINT NOT NULL,
                         name VARCHAR(100) NOT NULL,                 -- 반 명칭 (예: 중등 심화 A반)
                         target_grade VARCHAR(30),                   -- 대상 학년 (예: 중1, 중2)
                         max_students INT DEFAULT 0,                 -- 수강 정원 (0: 무제한)
                         status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE(운영중), CLOSED(종강) -> Soft Delete용
                         description VARCHAR(255),
                         created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                         CONSTRAINT fk_classes_academy FOREIGN KEY (academy_id) REFERENCES academies(academy_id)
);

CREATE TABLE class_teachers (
                                class_teacher_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                class_id BIGINT NOT NULL,
                                teacher_id BIGINT NOT NULL,
                                role_type VARCHAR(30) DEFAULT 'MAIN',       -- MAIN(담임), SUB(부담임), SUBJECT(과목강사)
                                created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT uk_class_teachers UNIQUE (class_id, teacher_id),
                                CONSTRAINT fk_class_teachers_class FOREIGN KEY (class_id) REFERENCES classes(class_id) ON DELETE CASCADE,
                                CONSTRAINT fk_class_teachers_teacher FOREIGN KEY (teacher_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE class_students (
                                class_student_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                class_id BIGINT NOT NULL,
                                student_id BIGINT NOT NULL,
                                status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE(수강중), DROPPED(퇴원/제외)
                                enrolled_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT uk_class_students UNIQUE (class_id, student_id),
                                CONSTRAINT fk_class_students_class FOREIGN KEY (class_id) REFERENCES classes(class_id) ON DELETE CASCADE,
                                CONSTRAINT fk_class_students_student FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE
);


-- =========================================================================================
-- [도메인 3] 시험 통합, OMR 고속 채점, 온라인 응시 및 성적 추이 (최서연 & 유은혜 최종 통합)
-- =========================================================================================

CREATE TABLE exams (
                       exam_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       class_id BIGINT NOT NULL,                           -- 소속 반 (FK: classes)
                       teacher_id BIGINT NOT NULL,                         -- 출제/등록 교사 (FK: users)
                       template_exam_id BIGINT,                            -- [은혜] 포맷 1초 복제 원본 ID (Self-FK)
                       title VARCHAR(150) NOT NULL,                        -- 시험 명칭
                       exam_type VARCHAR(20) NOT NULL,                     -- WORD, MONTHLY, REVIEW, OTHER
                       exam_mode VARCHAR(20) NOT NULL DEFAULT 'PAPER',     -- [서연] ONLINE(온라인 응시), PAPER(종이/OMR)
                       exam_round INT DEFAULT 1,                           -- [은혜] 꺾은선 성적 추이 회차/분기 (1, 2, 3...)
                       status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',        -- DRAFT, OPEN, CLOSED
                       exam_date DATE,                                     -- 시험 시행일
                       file_url VARCHAR(500),                              -- [서연] PDF 등 시험지 원본 파일 경로
                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                       CONSTRAINT fk_exams_class FOREIGN KEY (class_id) REFERENCES classes(class_id),
                       CONSTRAINT fk_exams_teacher FOREIGN KEY (teacher_id) REFERENCES users(user_id),
                       CONSTRAINT fk_exams_template FOREIGN KEY (template_exam_id) REFERENCES exams(exam_id) ON DELETE SET NULL
);

CREATE TABLE exam_sections (
                               section_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               exam_id BIGINT NOT NULL,                            -- 소속 시험 (FK: exams)
                               section_type VARCHAR(30) NOT NULL,                  -- VOCAB, GRAMMAR, READING 등
                               section_name VARCHAR(100) NOT NULL,                 -- 영역 명칭
                               max_score INT,
                               sort_order INT NOT NULL DEFAULT 1,

                               CONSTRAINT fk_exam_sections_exam FOREIGN KEY (exam_id) REFERENCES exams(exam_id) ON DELETE CASCADE
);

CREATE TABLE exam_questions (
                                question_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                exam_id BIGINT NOT NULL,                            -- 소속 시험 (FK: exams)
                                section_id BIGINT,                                  -- 소속 영역 (FK: exam_sections - 대분류)
                                question_number INT NOT NULL,                       -- [은혜] 문항 번호 (1~20번 OMR 매핑)
                                question_type VARCHAR(20) NOT NULL DEFAULT 'MULTIPLE_CHOICE', -- [서연] MULTIPLE_CHOICE, SHORT_ANSWER
                                question_type_tag VARCHAR(50) NOT NULL,             -- [은혜] 세부 유형 태그 (예: GRAMMAR_RELATIVE)
                                score DECIMAL(7,2) NOT NULL DEFAULT 5.00,           -- 문항 배점
                                correct_answer TEXT,                                -- 정답 번호 또는 텍스트
                                question_text TEXT NOT NULL,                        -- 문제 지문/본문
                                sort_order INT NOT NULL DEFAULT 1,
                                source_question_id BIGINT,                          -- [서연] 나선형 복습 퀘스트 원본 참조 (Self-FK)

                                CONSTRAINT uk_exam_question_no UNIQUE (exam_id, question_number),
                                CONSTRAINT fk_exam_questions_exam FOREIGN KEY (exam_id) REFERENCES exams(exam_id) ON DELETE CASCADE,
                                CONSTRAINT fk_exam_questions_section FOREIGN KEY (section_id) REFERENCES exam_sections(section_id) ON DELETE SET NULL,
                                CONSTRAINT fk_exam_questions_source FOREIGN KEY (source_question_id) REFERENCES exam_questions(question_id) ON DELETE SET NULL
);

CREATE TABLE exam_attempts (
                               attempt_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               exam_id BIGINT NOT NULL,                            -- 시험 식별자 (FK: exams)
                               student_id BIGINT NOT NULL,                         -- 수강생 (FK: users)
                               attempt_no INT NOT NULL DEFAULT 1,                  -- 응시 차수
                               entry_method VARCHAR(20) NOT NULL DEFAULT 'MANUAL', -- ONLINE(학생응시), MANUAL(강사 OMR 일괄입력)
                               status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',  -- IN_PROGRESS, SUBMITTED, GRADED
                               total_score DECIMAL(7,2) DEFAULT 0.00,              -- 자동 합산된 최종 총점
                               max_score DECIMAL(7,2) DEFAULT 100.00,              -- 총 만점 기준
                               correct_count INT DEFAULT 0,                        -- 맞힌 문항 수
                               total_question_count INT DEFAULT 0,                 -- 전체 문항 수
                               primary_weak_tag VARCHAR(50),                       -- 최다 오답 취약 유형 태그
                               started_at DATETIME,                                -- 시험 시작 시간
                               submitted_at DATETIME,                              -- 시험 제출 시간
                               graded_at DATETIME,                                 -- 채점 완료 시간

                               CONSTRAINT uk_exam_attempt UNIQUE (exam_id, student_id, attempt_no),
                               CONSTRAINT fk_exam_attempt_exam FOREIGN KEY (exam_id) REFERENCES exams(exam_id) ON DELETE CASCADE,
                               CONSTRAINT fk_exam_attempt_student FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE exam_answers (
                              answer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              attempt_id BIGINT NOT NULL,                         -- 응시 마스터 식별자 (FK: exam_attempts)
                              question_id BIGINT NOT NULL,                         -- 문항 식별자 (FK: exam_questions)
                              student_answer TEXT,                                -- 제출 답안
                              is_correct BOOLEAN DEFAULT FALSE,                   -- 정답 일치 여부 (O/X 자동 채점)
                              earned_score DECIMAL(7,2) DEFAULT 0.00,             -- 해당 문항 획득 배점
                              created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT uk_exam_answer UNIQUE (attempt_id, question_id),
                              CONSTRAINT fk_exam_answer_attempt FOREIGN KEY (attempt_id) REFERENCES exam_attempts(attempt_id) ON DELETE CASCADE,
                              CONSTRAINT fk_exam_answer_question FOREIGN KEY (question_id) REFERENCES exam_questions(question_id) ON DELETE CASCADE
);

CREATE TABLE exam_section_scores (
                                     section_score_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     attempt_id BIGINT NOT NULL,                         -- 응시 식별자 (FK: exam_attempts)
                                     section_id BIGINT NOT NULL,                         -- 영역 식별자 (FK: exam_sections)
                                     score DECIMAL(7,2) NOT NULL,                        -- 해당 영역 획득 점수
                                     max_score DECIMAL(7,2),                             -- 해당 영역 만점 배점

                                     CONSTRAINT uk_exam_section_scores UNIQUE (attempt_id, section_id),
                                     CONSTRAINT fk_exam_section_scores_attempt FOREIGN KEY (attempt_id) REFERENCES exam_attempts(attempt_id) ON DELETE CASCADE,
                                     CONSTRAINT fk_exam_section_scores_section FOREIGN KEY (section_id) REFERENCES exam_sections(section_id) ON DELETE CASCADE
);


-- =========================================================================================
-- [도메인 4] 수업 전 보완 신호 & 공공 API 강좌 연동 (담당 기능: 4.2.1)
-- =========================================================================================

CREATE TABLE class_weakness_signals (
                                        signal_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        class_id BIGINT NOT NULL,
                                        target_date DATE NOT NULL,                          -- 수업 일자
                                        warning_level VARCHAR(20) NOT NULL,                 -- DANGER(🔴), WARNING(🟡), NORMAL(🟢)
                                        top_weak_tag_1 VARCHAR(50),                         -- 1위 취약유형 (예: GRAMMAR_RELATIVE)
                                        top_weak_tag_2 VARCHAR(50),                         -- 2위 취약유형 (예: VOCAB_SYNONYM)
                                        weak_students_count INT NOT NULL DEFAULT 0,
                                        summary_message TEXT,                               -- 수업 전 1분 브리핑 문구
                                        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                        CONSTRAINT uk_class_weakness UNIQUE (class_id, target_date),
                                        CONSTRAINT fk_signal_class FOREIGN KEY (class_id) REFERENCES classes(class_id) ON DELETE CASCADE
);

CREATE TABLE student_curated_assignments (
                                             assignment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             student_id BIGINT NOT NULL,
                                             assigned_by BIGINT NOT NULL,
                                             weak_tag VARCHAR(50) NOT NULL,                      -- 매핑 취약유형
                                             provider VARCHAR(30) NOT NULL,                      -- EBS, K_MOOC
                                             course_title VARCHAR(200) NOT NULL,
                                             deep_link_url VARCHAR(500) NOT NULL,                -- 공식 수강창 이동 URL
                                             is_completed BOOLEAN NOT NULL DEFAULT FALSE,
                                             assigned_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                             CONSTRAINT fk_curated_student FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
                                             CONSTRAINT fk_curated_teacher FOREIGN KEY (assigned_by) REFERENCES users(user_id)
);


-- =========================================================================================
-- [도메인 5] 독서 및 교사 코칭 도메인 (유용수 님)
-- =========================================================================================

CREATE TABLE books (
                       book_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       title VARCHAR(200) NOT NULL,
                       author VARCHAR(100),
                       cover_image_url VARCHAR(500),
                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE reading_reports (
                                 reading_report_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 student_id BIGINT NOT NULL,
                                 book_id BIGINT NOT NULL,
                                 title VARCHAR(200),
                                 content TEXT NOT NULL,
                                 submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                 CONSTRAINT fk_reading_reports_student FOREIGN KEY (student_id) REFERENCES users(user_id),
                                 CONSTRAINT fk_reading_reports_book FOREIGN KEY (book_id) REFERENCES books(book_id)
);

CREATE TABLE reading_feedbacks (
                                   feedback_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                   reading_report_id BIGINT NOT NULL,
                                   teacher_id BIGINT NOT NULL,
                                   content VARCHAR(1000) NOT NULL,
                                   created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                   CONSTRAINT uk_reading_feedbacks_report UNIQUE (reading_report_id),
                                   CONSTRAINT fk_reading_feedbacks_report FOREIGN KEY (reading_report_id) REFERENCES reading_reports(reading_report_id) ON DELETE CASCADE,
                                   CONSTRAINT fk_reading_feedbacks_teacher FOREIGN KEY (teacher_id) REFERENCES users(user_id)
);


-- =========================================================================================
-- [도메인 7] 학생 자기성찰 및 학부모 발송 리포트 도메인 (★최신 스키마)
-- =========================================================================================

-- 1) 학생 월간 자기성찰 리포트 (학생 주도형 - 감정/Keep/Problem/Try 포함)
CREATE TABLE student_reports (
                                 report_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 student_id BIGINT NOT NULL,                         -- 대상 학생 (FK)
                                 period_start DATE NOT NULL,                         -- 4주(한 달) 시작일
                                 period_end DATE NOT NULL,                           -- 4주(한 달) 종료일

    -- [1. 자아성찰의 핵심: 나의 내면 돌아보기]
                                 monthly_mood VARCHAR(50),                           -- 이번 달 나의 전반적인 기분/감정 상태
                                 past_resolution TEXT,                               -- 4주 전 시작할 때 세웠던 목표 (과거의 나)
                                 proudest_moment TEXT,                               -- 가장 칭찬해주고 싶은 순간/행동 (Keep)
                                 habit_to_improve TEXT,                              -- 고치고 싶거나 버리고 싶은 나쁜 습관 (Problem)
                                 self_feedback TEXT,                                 -- 종합 셀프 피드백 일기
                                 next_resolution TEXT,                               -- 다음 4주를 위해 새롭게 다짐하는 목표 (Try - 미래의 나)
                                 self_effort_score TINYINT,                          -- 노력 만족도 별점 (1~5)

    -- [2. 성찰의 객관적 근거: 나의 행동 지표]
                                 books_read_count INT NOT NULL DEFAULT 0,            -- 읽은 도서 권수
                                 exam_completion_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00,   -- 시험 응시율 (%)
                                 retest_completion_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00, -- 재시험 응시율 (%)
                                 study_attendance_days INT NOT NULL DEFAULT 0,       -- 학습 참여 출석 일수
                                 overcome_wrong_count INT NOT NULL DEFAULT 0,        -- 스스로 극복해낸 오답 문제 수

    -- [3. 시스템 관리 메타데이터]
                                 created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP,
    -- [4. 아는 개념 / 모르는 개념]
                                 known_concepts TEXT,
                                 unknown_concepts TEXT,

                                 CONSTRAINT fk_student_reports_student FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE
);
-- 3. 지난 달 리포트 (report_id = 1) 가상 데이터 추가
INSERT INTO student_reports (student_id, period_start, period_end, books_read_count, exam_completion_rate, retest_completion_rate, study_attendance_days, overcome_wrong_count)
VALUES (999, '2026-06-01', '2026-06-28', 3, 88.00, 78.00, 20, 28);

-- 4. 이번 달 리포트 (report_id = 2) 가상 데이터 추가
INSERT INTO student_reports (student_id, period_start, period_end, books_read_count, exam_completion_rate, retest_completion_rate, study_attendance_days, overcome_wrong_count)
VALUES (999, '2026-07-01', '2026-07-28', 4, 92.00, 85.00, 24, 37);

-- 2) 학부모 월간 발송 리포트 (교사 발행 / 무로그인 웹뷰)
CREATE TABLE parent_reports (
                                report_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                student_id BIGINT NOT NULL,                         -- 대상 학생 (FK)
                                created_by BIGINT NOT NULL,                         -- 작성/발송 강사 (FK)
                                period_start DATE NOT NULL,                         -- 집계 시작일
                                period_end DATE NOT NULL,                           -- 집계 종료일
                                access_token VARCHAR(100) NOT NULL UNIQUE,          -- 학부모 웹뷰 접근용 보안 난수 토큰

    -- [성과 지표]
                                monthly_exam_score DECIMAL(5,2),                    -- 학생의 당월 월말평가 점수
                                class_average_score DECIMAL(5,2),                   -- 반 평균 점수
                                comprehension_level VARCHAR(50),                    -- 학습 이해도 (A, B, 매우 우수 등)

    -- [학습 태도 및 달성 지표]
                                word_exam_completion_rate DECIMAL(5,2),             -- 단어 시험 응시율
                                monthly_exam_completion_rate DECIMAL(5,2),          -- 월말평가 응시율
                                books_read_count INT NOT NULL DEFAULT 0,            -- 완독 도서 권수
                                overcome_wrong_count INT NOT NULL DEFAULT 0,        -- 오답 노트 극복 문제 수
                                top_weak_type_tag VARCHAR(50),                      -- 최다 오답 취약 유형 태그

    -- [서술형 평가]
                                teacher_comment TEXT,                               -- 학부모 전달용 교사 공식 코멘트

    -- [상태 및 시간 관리]
                                status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',        -- DRAFT(작성중), PUBLISHED(발송완료)
                                created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                published_at DATETIME,                              -- 학부모 발송 일시

                                CONSTRAINT fk_parent_reports_student FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
                                CONSTRAINT fk_parent_reports_creator FOREIGN KEY (created_by) REFERENCES users(user_id)
);
ALTER TABLE parent_reports
    ADD COLUMN radar_chart_data TEXT;

-- =========================================================================================
-- [도메인 8] 활동 로그 & 캐릭터 성장 도메인
-- =========================================================================================

CREATE TABLE exp_rules (
                           exp_rule_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           activity_type VARCHAR(50) NOT NULL UNIQUE,          -- EXAM, READING, TIP 등
                           exp_amount INT NOT NULL,
                           enabled BOOLEAN NOT NULL DEFAULT TRUE,
                           created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE activity_logs (
                               log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               student_id BIGINT NOT NULL,
                               activity_type VARCHAR(50) NOT NULL,
                               reference_id BIGINT,
                               earned_exp INT NOT NULL DEFAULT 0,
                               created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT fk_activity_logs_student FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE student_growth (
                                student_id BIGINT PRIMARY KEY,
                                total_exp INT NOT NULL DEFAULT 0,
                                character_stage INT NOT NULL DEFAULT 1,
                                updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                CONSTRAINT fk_student_growth_student FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE
);