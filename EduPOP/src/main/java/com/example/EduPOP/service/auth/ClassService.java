package com.example.EduPOP.service.auth;

import com.example.EduPOP.domain.user.AcademyClass;
import com.example.EduPOP.domain.user.ClassDetail;
import com.example.EduPOP.domain.user.StudentInfo;
import com.example.EduPOP.repository.user.ClassMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassService {
    private final ClassMapper classMapper;

    //학급 목록
    //클래스 테이블에서 academyId 찾아서 정렬
    public List<AcademyClass> findClassesByAcademyId(Long academyId) {
        return classMapper.findClassesByAcademyId(academyId);
    }

    //학급 상세
    //클래스 테이블에서 academyId, classId 찾아서 특정 학급상세정보 조회
    public ClassDetail findClassById(Long classId,
                                     Long academyId) {
        //학급명 조회
        ClassDetail classDetail = classMapper.findClassDetail(classId, academyId);
        if (classDetail == null) {
            return null;
        }
        //담임 교사 조회
        String teacherName = classMapper.findTeacherName(
                classId,
                academyId
        );
        //학급 학생 조회
        List<StudentInfo> students = classMapper.findStudent(
                classId,
                academyId
        );
        //학급의 선생님, 학생 넣기
        classDetail.setTeacherName(teacherName);
        classDetail.setStudents(students);

        return classDetail;
    }

 //---------------------------------------------------------
 //학급 수정
    public void updateClass(AcademyClass classInfo){
        classMapper.updateClass(classInfo);
    }
 //---------------------------------------------------------
 //학급 삭제
    public void deleteClass(Long classId,
                            Long academyId){
        //학생관계 삭제
        classMapper.deleteClassStudents(classId);

        //교사관계 삭제
        classMapper.deleteClassTeachers(classId);

        //학급 삭제
        classMapper.deleteClass(classId,academyId);
    }

}
