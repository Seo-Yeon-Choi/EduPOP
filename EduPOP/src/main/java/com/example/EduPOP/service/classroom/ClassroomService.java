package com.example.EduPOP.service.classroom;

import com.example.EduPOP.domain.classroom.ClassTeacher;
import com.example.EduPOP.domain.classroom.Classroom;
import com.example.EduPOP.repository.classroom.ClassroomMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClassroomService {
   private final ClassroomMapper classroomMapper;

    /**
     * 반 생성 및 다중 강사 배정
     * - 학원 내 반 이름 중복 검사
     * - classes 테이블 INSERT 및 자동 생성 PK 획득
     * - class_teachers 테이블에 배정 강사진 일괄 INSERT
     */
   @Transactional
    public Long createClass(Classroom classroom){
       // 동일 학원 내 중복 반 명칭 사전 검증
        Classroom existingClass = classroomMapper
                .findByNameAndAcademyId(classroom.getName(), classroom.getAcademyId());
        if(existingClass != null){
            throw new IllegalArgumentException("이미 동일한 이름의 반이 존재합니다.");
        }

       // 반 기본 정보 저장 (useGeneratedKeys로 classId 자동 세팅)
        classroomMapper.insertClass(classroom);

        // 배정된 강사진 매핑 데이터 저장 (N:M 해소)
        if(classroom.getTeachers() != null && !classroom.getTeachers().isEmpty()){
            for (ClassTeacher teacher : classroom.getTeachers()){
                teacher.setClassId(classroom.getClassId()); // 생성된 반 PK 주입
                classroomMapper.insertClassTeacher(teacher);
            }
        }
        return classroom.getClassId();
    }
}
