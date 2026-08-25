package com.example.EduPOP;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling //withdrawn 회원 1년 자동삭제를 위한 스케쥴 어노테이션
@SpringBootApplication
public class EduPopApplication {

	public static void main(String[] args) {
		SpringApplication.run(EduPopApplication.class, args); // Spring Boot 프로젝트 실행

}

}
