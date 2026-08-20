document.addEventListener("DOMContentLoaded", () => {
    const form = document.querySelector("#readingReportForm");
    const contentInput = document.querySelector("#reportContent");
    const contentLength = document.querySelector("#contentLength");
    const contentHelp = document.querySelector("#contentHelp");
    const characterCounter = document.querySelector(".character-counter");

    function updateCharacterCount() {
        const currentLength = contentInput.value.length;
        contentLength.textContent = currentLength.toLocaleString("ko-KR"); // 현재 독서록 글자 수 표시
        characterCounter.classList.toggle("limit-warning", currentLength >= 9_500); // 글자 제한이 가까우면 경고 표시
    }

    function clearContentError() {
        contentInput.classList.remove("input-error");
        contentHelp.classList.remove("error-text");
        contentHelp.textContent = "임시 저장은 50자 미만도 가능하지만, 최종 제출은 50자 이상 작성해야 합니다.";
    }

    contentInput.addEventListener("input", () => {
        updateCharacterCount();
        clearContentError();
    });

    form.addEventListener("submit", (event) => {
        const clickedButton = event.submitter;
        const isSubmitRequest = clickedButton && clickedButton.value === "SUBMITTED";
        const trimmedContentLength = contentInput.value.trim().length;

        if (isSubmitRequest && trimmedContentLength < 50) {
            event.preventDefault(); // 50자 미만인 독서록의 최종 제출 중단
            contentInput.classList.add("input-error");
            contentHelp.classList.add("error-text");
            contentHelp.textContent = "독서록을 제출하려면 내용을 50자 이상 작성해 주세요.";
            contentInput.focus();
        }
    });

    updateCharacterCount();
});
