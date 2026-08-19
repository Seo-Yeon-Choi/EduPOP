# EduPOP 개발 규칙

반드시 application.properties에 본인 DB 계정 입력
SQL Workbench로 edupop 데이터베이스 생성해야함
test.html은 테스트용 페이지임

## Package

기능별 코드는 다음 구조를 사용합니다.

시험 기능 예시

controller/exam
service/exam
domain/exam
repository/exam
resources/mapper/exam

## Naming

Controller
- XxxController

Service
- XxxService

Domain
- 테이블 단수형 이름

Mapper
- XxxMapper

Mapper XML
- XxxMapper.xml

## MyBatis

Mapper Interface:
repository/{feature}

Mapper XML:
resources/mapper/{feature}

## Branch

feature/{기능명}

예:
feature/exam
feature/reading
feature/report
