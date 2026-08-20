package com.example.EduPOP.domain.classroom;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 반(Classroom) 도메인
 * - classes 테이블 매핑
 * - 수강생 및 다중 강사 배정/관리
 */
@Data
@NoArgsConstructor
public class Classroom {
    private Long classId;  // 반 고유 식별자 (PK, AUTO_INCREMENT)
    private Long academyId; // 소속 학원 식별자 (FK)
    private String name; // 반 명칭 (학원 내 중복 방지 대상)
    private String targetGrade; // 대상 학년
    private Integer maxStudents; // 수강 정원 (정원 초과 방지 기준)
    private ClassStatus status = ClassStatus.ACTIVE; // 반 운영 상태 (ACTIVE: 운영중, CLOSED: 종강)
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // [상태 및 역할 정의용 Inner Enum]
    public enum ClassStatus {
        ACTIVE,  // 운영중
        CLOSED   // 종강 (Soft Delete - 데이터 무결성 보존용)
    }

    public enum ClassStudentStatus {
        ACTIVE,       // 수강중
        TRANSFERRED,  // 전반(다른 반으로 이동)
        DROPPED       // 제외/퇴원
    }

    public enum TeacherRoleType {
        MAIN, // 대표/담임 강사
        SUB   // 부담임/과목 강사
    }

    // 1:N 매핑 리스트
    private List<ClassTeacher> teachers = new ArrayList<>(); // 배정된 다중 강사진 목록
    private List<ClassStudent> students = new ArrayList<>(); // 등록된 수강생 목록

    /**
     * 반에 수강생 추가 (중복 배정 방지)
     */
    public void addStudent(Long studentId){
        // 이미 배정된 학생인지 중복 검사 (리스트 안의 객체들을 순회하며 확인)
        boolean isAlreadyAdded = this.students.stream()
                .anyMatch(cs -> cs.getStudentId().equals(studentId));

        // 중복이 아닐 때만 새로운 매핑 객체 생성 -> 리스트에 추가
        if(!isAlreadyAdded) {
            // 학생과 반 연결할 객체 생성
            ClassStudent classStudent = new ClassStudent();
            // 이 객체에 현재 반의 고유번호(this.classId) 와 학생번호 (studentId) 담기
            classStudent.setClassId(this.classId);
            classStudent.setStudentId(studentId);
            classStudent.activate(); // 기본 상태 값
            this.students.add(classStudent);
        }
    }

    /**
     * 반에서 수강생 제외
     */
    public void removeStudent(Long studentId){
        this.students.removeIf(cs -> cs.getStudentId().equals(studentId));
    }

    /**
     * 다중 강사 배정 (중복 배정 방지 및 기본 역할 부여)
     * @param roleType 미지정(null) 시 기본값 SUB 설정
     */
    public void assignTeacher(Long teacherId, TeacherRoleType roleType){
        // 배정 된 선생님인지 중복 검사
        boolean isAlreadyAdded = this.teachers.stream()
                .anyMatch(ct ->ct.getTeacherId().equals(teacherId));

        // 중복이 아닐 때만 새로운 매핑 객체 생성 -> 리스트에 추가
        if(!isAlreadyAdded) {
            // 강사와 반 연결할 객체
            ClassTeacher classTeacher = new ClassTeacher();
            // 이 객체에 현재 반의 고유번호(this.classId) 와 강사번호 (teacherId) 담기
            classTeacher.setClassId(this.classId);
            classTeacher.setTeacherId(teacherId);
            // 강사 role이 null일 경우만 "SUB" 로 세팅
            classTeacher.setRoleType(roleType != null ? roleType : TeacherRoleType.SUB);
            // 완성된 강사 객체를 현재 반의 강사 목록 주머니(teachers 리스트)에 넣기
            this.teachers.add(classTeacher);
        }
    }

    /**
     * 반 종강 처리 (성적/출결 이력 보존을 위한 Soft Delete)
     */
    public void close(){
        this.status = ClassStatus.CLOSED;
    }
}
