package com.example.EduPOP.service.classroom;

import com.example.EduPOP.domain.user.AcademyClass;
import com.example.EduPOP.repository.classroom.ClassMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassService {

    private final ClassMapper classMapper;


    public List<AcademyClass> getClassesByTeacher(
            Long teacherId) {

        return classMapper.findByTeacherId(
                teacherId
        );
    }
}