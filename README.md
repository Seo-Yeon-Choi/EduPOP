<p align="center">
  <img src="./EduPOP/src/main/resources/static/images/exp/character.png" alt="EduPOP 성장 캐릭터" width="150">
</p>

<h1 align="center">EduPOP</h1>

<p align="center"><strong>학원 운영과 시험 결과를 다음 학습으로 연결하는 교육 플랫폼</strong></p>

<p align="center">시험 → 분석 → 복습 → 성장 리포트의 흐름을 하나의 서비스로 연결해, 교사는 다음 수업을 준비하고 학생은 자신의 취약점을 확인할 수 있도록 설계했습니다.</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=openjdk&logoColor=white" alt="Java 17">
  <img src="https://img.shields.io/badge/Spring_Boot-4.1.0-6DB33F?style=flat-square&logo=springboot&logoColor=white" alt="Spring Boot 4.1.0">
  <img src="https://img.shields.io/badge/MyBatis-4.0.1-000000?style=flat-square" alt="MyBatis 4.0.1">
  <img src="https://img.shields.io/badge/MySQL-8.x-4479A1?style=flat-square&logo=mysql&logoColor=white" alt="MySQL">
  <img src="https://img.shields.io/badge/Thymeleaf-3.x-005F0F?style=flat-square&logo=thymeleaf&logoColor=white" alt="Thymeleaf">
  <img src="https://img.shields.io/badge/OpenAI_API-412991?style=flat-square&logo=openai&logoColor=white" alt="OpenAI API">
</p>

---

## 👤 My Contribution

> **아이디어 제안부터 기능 기획, 데이터 흐름 설계, 구현 및 기능 간 재사용 구조까지 참여했습니다.**
>
> 전체 프로젝트에서 한 기능만 개발한 것이 아니라, **교사가 실제 수업 전에 무엇을 확인해야 하는가**를 기준으로 기능을 구체화하고 이를 코드와 데이터 흐름으로 연결했습니다.

### 1. 서비스 기획 및 핵심 기능 설계

EduPOP 아이디어를 제안하고 팀원들과 서비스의 전체 방향을 구체화했습니다.

- 교사용 **수업 전 3분 보완 신호 대시보드** 기획
- 반별 성취도와 전체 평균 비교
- 반 전체 취약 유형 분석
- 학생별 오답률 및 취약 유형 확인
- 위험도에 따른 🔴 위험 / 🟡 주의 / 🟢 안정 신호 설계
- 학생별 상세 리포트로 이어지는 정보 구조 설계
- 개별 학생 성적 분석 화면 및 시각화 구성
- 분석 결과를 다른 리포트에서도 재사용할 수 있도록 내부 API/메서드 구조 설계

### 2. 반 생성 및 학생·강사 배정

학원 운영에서 필요한 **반 생성 → 담당 강사 배정 → 학생 배정** 흐름을 설계하고 구현했습니다.

- 반 생성 및 반 정보 관리
- 한 반에 여러 강사를 배정할 수 있는 구조
- 학생의 반 배정 및 변경
- 중복 배정 방지
- 기존 이력에 영향을 주지 않도록 상태 기반 관리
- classes, class_teachers, class_students 관계를 기준으로 데이터 구조 설계
- Controller → Service → MyBatis Mapper로 이어지는 계층 구조 구현

관련 코드:
- controller/classroom
- service/classroom
- repository/classroom
- domain/classroom
- templates/classroom
- DB/schema.sql

### 3. 수업 전 3분 보완 신호 대시보드

교사가 수업 직전에 빠르게 확인할 수 있도록 **반 단위 분석을 한 화면에 모으는 기능**을 구현했습니다.

| 분석 대상 | 제공 정보 |
| --- | --- |
| 반 성취도 | 최근 시험의 우리 반 평균 vs 전체 평균 |
| 반 취약 영역 | 반 전체에서 정답률이 낮은 유형 |
| 학생 위험 신호 | 학생별 오답률 기반 상태 |
| 학생 취약 유형 | 학생별 취약 유형 및 취약 단어 |
| 수업 준비 | 다음 수업에서 확인할 학생과 영역을 빠르게 파악 |

반을 선택하면 API를 호출해 분석 데이터를 받아오고, 결과에 따라 상태 신호와 비교 정보를 동적으로 표시합니다.

~~~text
교사
 ↓
반 선택
 ↓
/analytics/api/class/{classId}/warning-signal
 ↓
반 시험 성취도 비교
 + 반 취약 유형
 + 학생별 오답률
 + 학생별 취약 유형
 ↓
수업 전 보완 대상 확인
~~~

### 4. 학생 개인 성적 분석 및 데이터 재사용

학생별 시험 이력과 영역별 성취도를 조회하고 시각화하는 분석 기능을 구현했습니다.

- 시험 회차별 성적 추이
- 영역별 성취도
- 소분류별 성취도
- 강점 Top 3
- 취약 유형 Worst 3
- 데이터가 없는 경우를 고려한 Null-Safe 처리
- 분석 데이터를 다른 리포트 기능에서도 사용할 수 있도록 메서드 단위로 분리

특히 **3분 대시보드와 개인 성적 분석에서 만들어진 분석 데이터를 다른 팀원의 월간 학생 리포트에서도 재사용할 수 있도록 내부 API/메서드 구조를 연결**했습니다.

같은 성적 계산 로직을 화면마다 다시 구현하기보다 **분석 데이터를 재사용 가능한 데이터 소스로 만드는 방향**으로 설계했습니다.

---

## 📌 Project Overview

EduPOP은 초·중학생 대상 학원의 운영과 학습 과정을 하나의 웹 서비스로 연결한 팀 프로젝트입니다.

### 핵심 학습 순환

~~~mermaid
flowchart LR
    A["학원 운영"] --> B["시험 생성·응시"]
    B --> C["채점·성적 분석"]
    C --> D["오답·AI·게임 복습"]
    D --> E["성장 리포트"]
    E -. "다음 학습" .-> B
~~~

### 사용자별 주요 기능

| 사용자 | 주요 기능 |
| --- | --- |
| 관리자 | 학원 등록 및 검증, 회원 승인·관리, 반 생성·관리, 강사·학생 배정 |
| 교사 | 시험 제작, PDF 문제 추출, OMR 채점, 성적 분석, 3분 대시보드, 학생 리포트, 독서 피드백 |
| 학생 | 시험 응시, 오답 확인, 오늘의 복습, AI 유사 문제, 단어 게임, 독서 활동, 성장 리포트 |

## 🔎 주요 기능

### 시험 제작과 채점
- 직접 입력하거나 기존 시험 템플릿을 복사해 시험지를 생성합니다.
- Apache PDFBox를 이용해 PDF의 텍스트를 추출하고 문항을 파싱합니다.
- 객관식·주관식 문제와 대분류·소분류를 관리합니다.
- 온라인 응시 결과와 교사의 OMR 입력 결과를 저장합니다.

### 맞춤형 복습
- 최근 오답을 기반으로 오늘의 복습 문제를 제공합니다.
- OpenAI API를 이용해 동일 개념의 유사 문제를 생성합니다.
- 생성 결과의 문항 수, 선택지 수, 정답 범위, 원본 중복 여부 등을 검증합니다.
- 단어 복습을 게임 형태로 제공합니다.

### 성적 분석과 리포트
- Chart.js를 이용해 시험 점수와 영역별 성취도를 시각화합니다.
- 대분류·소분류별 성취도와 취약 유형을 분석합니다.
- 학생별 성적 추이와 강점·취약 영역을 제공합니다.
- 월간 학습 리포트를 통해 시험, 복습, 독서 활동을 종합합니다.

### 인증과 보안
- 일반 로그인 및 Kakao·Naver·Google 소셜 로그인을 지원합니다.
- Spring Security를 이용해 관리자·교사·학생의 접근 권한을 분리합니다.
- 비밀번호는 BCrypt로 암호화합니다.
- 세션 및 CSRF 보호를 적용합니다.

### 학원 등록 검증
- 국세청 사업자등록정보 진위확인 API를 이용해 학원 등록 정보를 검증합니다.
- 사업자번호, 대표자명, 개업일자 및 영업 상태를 확인합니다.

---

## 🧩 기술 스택

| 영역 | 기술 |
| --- | --- |
| Backend | Java 17, Spring Boot 4.1.0, Spring MVC, Spring Security |
| Persistence | MyBatis, MySQL |
| Frontend | Thymeleaf, HTML5, CSS3, JavaScript |
| Visualization | Chart.js |
| AI | OpenAI Java SDK |
| Document | Apache PDFBox |
| Authentication | Session, OAuth 2.0 Authorization Code, BCrypt |
| External API | 국세청 사업자등록정보 진위확인 API |
| Build | Maven Wrapper, Lombok |

> 참고: 실제 pom.xml에는 Spring Data JPA도 포함되어 있고 일부 Report 도메인에서 JPA 관련 코드가 확인됩니다. 다만 **주요 데이터 접근 및 제가 담당한 분석·반 관리 기능은 MyBatis 기반**으로 구현했습니다.

## 🏗️ 애플리케이션 구조

~~~mermaid
flowchart TB
    U["관리자 · 교사 · 학생"] --> V["Thymeleaf View"]
    V --> S["Spring MVC · Spring Security"]
    S --> B["Controller · Service"]
    B --> D["MyBatis · MySQL"]
    B --> X["OAuth · OpenAI · 국세청 API"]
    B --> P["PDFBox · Chart.js"]
~~~

### 분석 기능 데이터 흐름

~~~text
Controller
    ↓
AnalyticsService
    ↓
AnalyticsMapper
    ↓
MySQL
    ↓
StudentTrendResponse / ClassWarningResponse
    ↓
Thymeleaf + JavaScript
    ↓
차트 · 비교 지표 · 위험 신호
~~~

## 💡 설계 포인트

### 분석 기능의 재사용
시험 성적 데이터를 화면마다 개별적으로 계산하는 대신 분석 로직을 Service/Mapper 단위로 분리하여 다른 리포트 기능에서 재사용할 수 있도록 구성했습니다.

### 반 단위 분석에서 학생 단위 분석으로 연결

~~~text
반 성취도
   ↓
반 취약 유형
   ↓
취약 학생 확인
   ↓
학생별 취약 유형
   ↓
개인 성적 분석
   ↓
다음 학습/보완 방향
~~~

### 운영 데이터의 관계 구조

~~~text
Academy
  └── Class
       ├── Teachers (N:M)
       └── Students (N:M)
~~~

반과 학생·강사의 관계를 별도 매핑 테이블로 관리하여 한 반에 여러 강사를 연결하고 학생의 반 소속을 관리할 수 있도록 했습니다.

---

## 🎬 Portfolio & Demo

기획자 관점에서 정리한 상세 포트폴리오 PDF와 실제 서비스 시연 영상은 파일 업로드 후 연결할 예정입니다.

- 📄 **기획자 관점 포트폴리오** — 문제 정의, 사용자 흐름, 기능 기획, 시장·경쟁 관점, 핵심 기능 및 구현 연결
- 🎥 **서비스 시연 영상** — 실제 구현된 주요 사용자 흐름과 핵심 기능

> 업로드 예정 경로: docs/EduPOP_Planning_Portfolio.pdf / docs/EduPOP_Demo.mp4

---

## 🚀 시작하기

### 요구 사항
- JDK 17
- MySQL 8.x
- Git
- 외부 연동 기능 사용 시 각 서비스의 API 키와 OAuth 애플리케이션

### 저장소 복제
~~~bash
git clone https://github.com/Seo-Yeon-Choi/EduPOP.git
cd EduPOP/EduPOP
~~~

### 데이터베이스 준비
MySQL에서 edupop 데이터베이스를 만든 뒤 DB/schema.sql을 적용합니다.

~~~sql
CREATE DATABASE edupop
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
~~~

### 환경 변수

| 환경 변수 | 용도 |
| --- | --- |
| OPENAI_API_KEY | AI 유사 문제 생성 |
| KAKAO_CLIENT_ID | Kakao 로그인 |
| KAKAO_REDIRECT_URI | Kakao 콜백 주소 |
| NAVER_CLIENT_ID | Naver 로그인 |
| NAVER_CLIENT_SECRET | Naver 로그인 |
| NAVER_REDIRECT_URI | Naver 콜백 주소 |
| GOOGLE_CLIENT_ID | Google 로그인 |
| GOOGLE_CLIENT_SECRET | Google 로그인 |
| GOOGLE_REDIRECT_URI | Google 콜백 주소 |
| NTS_BUSINESS_API_KEY | 국세청 사업자등록정보 검증 |

API 키와 Client Secret은 저장소에 커밋하지 말고 환경 변수 또는 IntelliJ 실행 구성으로 주입합니다.

### 실행
~~~powershell
.\mvnw.cmd spring-boot:run
~~~

실행 후 http://localhost:8080으로 접속합니다.

---

<p align="center"><strong>EduPOP — 시험의 끝을 다음 성장의 시작으로</strong></p>