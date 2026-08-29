-- 1. 박시은 학생 세팅 (이름, 번호 2222)
INSERT IGNORE INTO users (user_id, name, phone, role)
VALUES (999, '박시은', '010-1111-2222', 'STUDENT');
UPDATE users SET phone = '010-1111-2222', name = '박시은' WHERE user_id = 999;

-- 2. 학부모 정보 세팅 및 연결 (현재 백엔드가 학부모 번호를 찾으므로 필수!)
INSERT IGNORE INTO users (user_id, name, phone, role)
VALUES (1000, '박시은어머니', '010-9999-2222', 'PARENT');
INSERT IGNORE INTO parent_students (parent_id, student_id)
VALUES (1000, 999);

-- 3. ★핵심★ 박시은 학생의 '발행 완료(PUBLISHED)' 리포트를 강제로 1개 생성!
INSERT IGNORE INTO parent_reports (report_id, student_id, status, access_token, created_at)
VALUES (99, 999, 'PUBLISHED', 'tk_park_1004', NOW());