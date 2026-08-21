package com.example.EduPOP.repository.user;

import com.example.EduPOP.domain.user.Academy;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AcademyMapper {
    void save(Academy academy);
}
