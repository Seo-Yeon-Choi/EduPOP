# 소셜 로그인 환경 변수 설정

로컬 환경(localhost:8080)에서 네이버 및 구글 소셜 로그인을 테스트하기 위한 환경 변수 설정 방법입니다.

## ⚙️ 설정 순서

1. 상단 메뉴에서 **Run** ➔ **Edit Configurations**를 클릭합니다.
2. 설정 창 우측 상단(또는 중간)에 있는 **Modify Options**를 클릭합니다.
3. **Operating System** 항목에서 **Environment Variables**를 선택합니다.
4. 아래의 환경 변수 값을 복사하여 입력란에 붙여넣습니다.

## 📝 환경 변수 입력값

> **주의:** `{발급받은 Client ID}`와 `{발급받은 Client Secret}` 부분은 실제 애플리케이션에서 발급받은 키 값으로 변경하여 입력해 주세요. (각 항목은 세미콜론(`;`)으로 구분됩니다.)

```text
NAVER_CLIENT_ID={발급받은 Client ID};NAVER_CLIENT_SECRET={발급받은 Client Secret};NAVER_REDIRECT_URI=http://localhost:8080/naver/callback;GOOGLE_CLIENT_ID={발급받은 Client ID};GOOGLE_CLIENT_SECRET={발급받은 Client Secret};GOOGLE_REDIRECT_URI=http://localhost:8080/google/callback