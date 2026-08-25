// HTML 화면 구성이 끝나면 독서 화면 기능 실행
document.addEventListener("DOMContentLoaded", () => {

    // 글자 수를 확인해야 하는 모든 입력창 조회
    const characterInputs =
        document.querySelectorAll("[data-character-input]");

    characterInputs.forEach((input) => {

        // 현재 입력창이 들어 있는 입력 영역 조회
        const formGroup =
            input.closest(".form-group");

        // 현재 글자 수를 표시할 부분 조회
        const countValue =
            formGroup?.querySelector("[data-count-value]");

        // 입력된 글자 수를 화면에 표시하는 기능
        const updateCharacterCount = () => {

            if (countValue) {
                countValue.textContent =
                    input.value.length.toLocaleString("ko-KR");
            }
        };

        // 사용자가 내용을 입력할 때마다 글자 수 갱신
        input.addEventListener(
            "input",
            updateCharacterCount
        );

        // 수정 화면처럼 기존 내용이 있을 때 처음 글자 수 표시
        updateCharacterCount();
    });


    // 도서 선택 카드의 라디오 버튼 모두 조회
    const bookRadios =
        document.querySelectorAll("[data-book-id]");

    // 감상문 작성 폼의 도서 선택창 조회
    const bookSelect =
        document.querySelector("#selectedBook");

    bookRadios.forEach((radio) => {

        // 사용자가 도서 카드를 선택하면 실행
        radio.addEventListener("change", () => {

            if (bookSelect) {

                // 카드의 도서 번호를 감상문 폼의 선택창에 반영
                bookSelect.value = radio.value;
            }
        });
    });


    // 삭제 확인이 필요한 모든 폼 조회
    const confirmForms =
        document.querySelectorAll("form[data-confirm]");

    confirmForms.forEach((form) => {

        // 삭제 요청을 서버로 보내기 직전에 실행
        form.addEventListener("submit", (event) => {

            // HTML의 data-confirm에 저장된 확인 문구 조회
            const message =
                form.dataset.confirm;

            // 사용자가 취소를 누르면 삭제 요청 중단
            if (message && !window.confirm(message)) {
                event.preventDefault();
            }
        });
    });
});