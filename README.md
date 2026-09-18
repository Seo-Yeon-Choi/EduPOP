<p align="center">
  <img src="./EduPOP/src/main/resources/static/images/exp/character.png" alt="EduPOP 성장 캐릭터" width="150">
</p>

<h1 align="center">EduPOP</h1>

<p align="center">
  <strong>학원 운영부터 시험, 복습, 분석, 리포트, 학생의 성장까지 하나의 흐름으로 연결한 교육 플랫폼</strong>
</p>

<p align="center">
  시험 점수에서 학습을 끝내지 않고, 결과를 다음 학습으로 이어 학생의 꾸준한 성장을 돕습니다.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=openjdk&logoColor=white" alt="Java 17">
  <img src="https://img.shields.io/badge/Spring_Boot-4.1.0-6DB33F?style=flat-square&logo=springboot&logoColor=white" alt="Spring Boot 4.1.0">
  <img src="https://img.shields.io/badge/MyBatis-4.0.1-000000?style=flat-square" alt="MyBatis 4.0.1">
  <img src="https://img.shields.io/badge/MySQL-8.x-4479A1?style=flat-square&logo=mysql&logoColor=white" alt="MySQL">
  <img src="https://img.shields.io/badge/Thymeleaf-3.x-005F0F?style=flat-square&logo=thymeleaf&logoColor=white" alt="Thymeleaf">
  <img src="https://img.shields.io/badge/OpenAI-API-412991?style=flat-square&logo=openai&logoColor=white" alt="OpenAI API">
</p>

---

## 프로젝트 소개

EduPOP은 초·중학생 대상 학원에서 이루어지는 운영과 학습을 통합한 웹 플랫폼입니다.

관리자는 학원과 회원, 반을 관리하고 교사는 시험 제작·채점·분석과 독서 피드백을 수행합니다. 학생은 시험 응시 후 오답 복습, AI 유사 문제, 게임형 복습, 독서 활동과 성장 리포트를 통해 학습 결과를 다음 행동으로 연결할 수 있습니다.

## 핵심 학습 순환

~~~mermaid
flowchart LR
    A["학원 운영"] --> B["시험 생성·응시"]
    B --> C["채점·성적 분석"]
    C --> D["오답·AI·게임 복습"]
    D --> E["경험치·성장 리포트"]
    E -. "다음 학습" .-> B
~~~

## 사용자별 주요 기능

| 사용자 | 주요 기능 |
| --- | --- |
| 관리자 | 사업자등록정보 검증을 통한 학원 등록, 회원 승인 및 관리, 반 생성·종강·재개, 강사와 학생 배정 |
| 교사 | 시험 분류 관리, 시험지 생성·조회·수정, PDF 문제 추출, OMR 일괄 채점, 학생별 코멘트, 반·학생 성적 및 취약 유형 분석, 독서감상문 피드백 |
| 학생 | 일반·단어 시험 응시, 결과 및 오답 확인, 오늘의 복습, AI 유사 문제, 단어 게임형 복습, 독서감상문 작성, 월간 학습 리포트, 경험치와 캐릭터 성장 |

## 주요 기능

### 시험 제작과 채점

- 직접 입력하거나 기존 시험 템플릿을 복사해 시험지를 생성합니다.
- Apache PDFBox로 PDF의 텍스트를 추출한 뒤 일반 시험과 단어 시험 형식에 맞게 문항을 파싱합니다.
- 객관식·주관식 문제와 대분류·소분류를 관리합니다.
- 온라인 응시 결과와 교사의 OMR 입력 결과를 저장하고 학생 결과 화면에 반영합니다.

### 맞춤형 복습

- 최근 오답을 모아 오늘의 복습 문제를 제공합니다.
- 객관식 오답을 기반으로 OpenAI API가 동일 개념의 유사 문제를 생성합니다.
- 생성 결과의 문항 수, 선택지 수, 정답 범위, 원본 중복 여부를 검증하고 이미 생성한 문제는 재사용합니다.
- 낙하형, 매칭, 미로, 디펜스 형식의 단어 복습 게임을 제공합니다.

### 학습 분석과 성장

- Chart.js로 시험 점수 추이와 영역별 정답률을 시각화합니다.
- 대분류·소분류별 성취도와 취약 유형을 분석해 교사의 학습 지도를 돕습니다.
- 시험, 복습, 독서 활동에 경험치를 지급하고 누적 경험치에 따라 캐릭터가 성장합니다.
- 월별 시험 응시율, 재시험 응시율, 학습 일수, 독서량, 극복한 오답 수를 리포트로 제공합니다.

### 인증과 보안

- 일반 로그인과 Kakao·Naver·Google 소셜 로그인을 지원합니다.
- 최초 소셜 로그인 시 학원 선택과 관리자 승인 절차를 거칩니다.
- Spring Security에서 관리자·교사·학생 역할별 URL 접근 권한을 분리합니다.
- 비밀번호는 BCrypt로 암호화하고 인증 정보는 서버 세션의 SecurityContext에 저장합니다.
- 로그인 시 세션 ID를 교체하고 로그아웃 시 세션과 JSESSIONID 쿠키를 제거합니다.
- CSRF 보호를 적용해 상태 변경 요청을 검증합니다.

### 학원 등록 검증

- 국세청 사업자등록정보 진위확인 API로 사업자번호, 대표자명, 개업일자를 검증합니다.
- 진위확인 후 현재 영업 상태까지 확인해 유효한 학원 등록만 처리합니다.

## 기술 스택

| 영역 | 기술 |
| --- | --- |
| Backend | Java 17, Spring Boot 4.1.0, Spring MVC, Spring Security |
| Persistence | MyBatis, Spring Data JPA, MySQL |
| Frontend | Thymeleaf, HTML5, CSS3, JavaScript |
| Visualization | Chart.js |
| AI | OpenAI Java SDK, GPT-5 mini, Structured Output |
| Document | Apache PDFBox |
| Authentication | Session, OAuth 2.0 Authorization Code, BCrypt |
| External API | 국세청 사업자등록정보 진위확인 API |
| Build | Maven Wrapper, Lombok |

## 애플리케이션 구조

~~~mermaid
flowchart TB
    U["관리자 · 교사 · 학생"] --> V["Thymeleaf View"]
    V --> S["Spring MVC · Spring Security"]
    S --> B["Controller · Service"]
    B --> D["MyBatis · JPA · MySQL"]
    B --> X["OAuth · OpenAI · 국세청 API"]
    B --> P["PDFBox · Chart.js"]
~~~

## 프로젝트 구조

~~~text
EduPOP/
├── README.md
└── EduPOP/
    ├── DB/
    │   └── schema.sql
    ├── pom.xml
    └── src/
        ├── main/
        │   ├── java/com/example/EduPOP/
        │   │   ├── config/          # 보안 및 웹 설정
        │   │   ├── controller/      # 요청 처리
        │   │   ├── service/         # 비즈니스 로직
        │   │   ├── repository/      # MyBatis Mapper와 JPA Repository
        │   │   ├── domain/          # 도메인 모델
        │   │   └── dto/             # 계층 간 데이터 전달
        │   └── resources/
        │       ├── mapper/          # MyBatis SQL
        │       ├── static/          # CSS, JavaScript, 이미지
        │       ├── templates/       # Thymeleaf 화면
        │       ├── application.properties
        │       └── application.yml
        └── test/
~~~

## 시작하기

### 1. 요구 사항

- JDK 17
- MySQL 8.x
- Git
- 외부 연동 기능 사용 시 각 서비스의 API 키와 OAuth 애플리케이션

### 2. 저장소 복제

~~~bash
git clone https://github.com/Seo-Yeon-Choi/EduPOP.git
cd EduPOP/EduPOP
~~~

### 3. 데이터베이스 준비

MySQL에서 edupop 데이터베이스를 만든 뒤 스키마를 적용합니다.

~~~sql
CREATE DATABASE edupop
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
~~~

~~~bash
mysql -u root -p edupop < DB/schema.sql
~~~

이후 **src/main/resources/application.properties**의 데이터베이스 계정 정보를 로컬 환경에 맞게 수정합니다.

### 4. 환경 변수 설정

| 환경 변수 | 용도 |
| --- | --- |
| OPENAI_API_KEY | AI 유사 문제 생성 |
| KAKAO_CLIENT_ID | Kakao 로그인 |
| KAKAO_REDIRECT_URI | Kakao 콜백 주소, 기본값은 localhost |
| NAVER_CLIENT_ID | Naver 로그인 |
| NAVER_CLIENT_SECRET | Naver 로그인 |
| NAVER_REDIRECT_URI | Naver 콜백 주소, 기본값은 localhost |
| GOOGLE_CLIENT_ID | Google 로그인 |
| GOOGLE_CLIENT_SECRET | Google 로그인 |
| GOOGLE_REDIRECT_URI | Google 콜백 주소, 기본값은 localhost |
| NTS_BUSINESS_API_KEY | 국세청 사업자등록정보 검증 |

API 키와 Client Secret은 저장소에 커밋하지 말고 IntelliJ 실행 구성이나 운영체제 환경 변수로 주입합니다.

### 5. 애플리케이션 실행

Windows:

~~~powershell
.\mvnw.cmd spring-boot:run
~~~

macOS / Linux:

~~~bash
./mvnw spring-boot:run
~~~

실행 후 브라우저에서 **http://localhost:8080**으로 접속합니다.

## 설계 포인트

- Controller–Service–Repository 계층을 분리해 화면 처리와 비즈니스 로직, 데이터 접근 책임을 구분했습니다.
- 시험 생성부터 응시, 채점, 분석, 복습까지 하나의 데이터 흐름으로 연결했습니다.
- 사용자 역할과 승인 상태를 함께 확인해 화면과 기능 접근을 제어합니다.
- AI 결과를 그대로 저장하지 않고 구조화 응답과 후처리 검증을 거쳐 데이터 정합성을 확보합니다.
- 경험치 중복 지급과 AI 문제 중복 생성을 방지해 외부 API 비용과 학습 기록 오류를 줄였습니다.

---

<p align="center">
  <strong>EduPOP — 시험의 끝을 다음 성장의 시작으로</strong>
</p>
