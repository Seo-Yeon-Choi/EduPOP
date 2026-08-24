document.addEventListener("DOMContentLoaded", () => {

    const submitButton =
        document.getElementById("submitExamButton");

    if (submitButton) {
        submitButton.addEventListener(
            "click",
            () => submitExam(false)
        );
    }

    startExamTimer();
});

let timerInterval = null;
let isSubmitting = false;

function startExamTimer() {

    const wordExamInput =
        document.getElementById("wordExam");

    if (!wordExamInput) {
        return;
    }

    const isWordExam =
        wordExamInput.value === "true";

    // 복습시험이면 타이머 사용 안 함
    if (!isWordExam) {
        return;
    }

    const attemptId =
        document.getElementById("attemptId").value;

    const storageKey =
        `examDeadline-${attemptId}`;

    // 타이머 시간 조절하는 부분
    const timeLimit =
        15 * 60 * 1000;

    let deadline =
        sessionStorage.getItem(storageKey);

    if (!deadline) {

        deadline =
            Date.now() + timeLimit;

        sessionStorage.setItem(
            storageKey,
            deadline
        );
    } else {

        deadline =
            Number(deadline);
    }

    updateTimer(deadline, storageKey);

    timerInterval =
        setInterval(() => {

            updateTimer(
                deadline,
                storageKey
            );

        }, 1000);
}

function updateTimer(deadline, storageKey) {

    const timerDisplay =
        document.getElementById("timerDisplay");

    if (!timerDisplay) {
        return;
    }

    const remaining =
        deadline - Date.now();

    if (remaining <= 0) {

        timerDisplay.textContent =
            "00:00";

        clearInterval(timerInterval);

        sessionStorage.removeItem(
            storageKey
        );

        alert(
            "시험 시간이 종료되었습니다.\n현재까지 작성한 답안이 자동으로 제출됩니다."
        );

        submitExam(true);

        return;
    }

    const totalSeconds =
        Math.ceil(remaining / 1000);

    const minutes =
        Math.floor(totalSeconds / 60);

    const seconds =
        totalSeconds % 60;

    timerDisplay.textContent =
        `${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`;

    // 1분 미만이면 경고 표시
    if (totalSeconds <= 60) {

        const timer =
            document.getElementById("examTimer");

        if (timer) {
            timer.classList.add(
                "timer-warning"
            );
        }
    }
}

async function submitExam(autoSubmit) {

    if (isSubmitting) {
        return;
    }

    const questionCards =
        document.querySelectorAll(
            ".question-card"
        );

    const answers = [];

    for (const card of questionCards) {

        const questionId =
            Number(card.dataset.questionId);

        const radio =
            card.querySelector(
                "input[type='radio']:checked"
            );

        const textInput =
            card.querySelector(
                ".student-answer"
            );

        let studentAnswer = "";

        if (radio) {
            studentAnswer = radio.value;
        }

        if (textInput) {
            studentAnswer =
                textInput.value.trim();
        }

        // 직접 제출하는 경우에만
        // 미응답 문제 체크
        if (!autoSubmit && !studentAnswer) {

            alert(
                "모든 문제에 답을 입력해주세요."
            );

            return;
        }

        // 자동 제출에서는 빈 문자열도 그대로 전송
        answers.push({
            questionId,
            studentAnswer
        });
    }

    if (!autoSubmit) {

        const confirmed =
            confirm(
                "시험을 제출하시겠습니까?\n제출 후에는 답안을 수정할 수 없습니다."
            );

        if (!confirmed) {
            return;
        }
    }

    isSubmitting = true;

    const submitButton =
        document.getElementById(
            "submitExamButton"
        );

    if (submitButton) {

        submitButton.disabled = true;

        submitButton.textContent =
            "제출 중...";
    }

    const attemptId =
        Number(
            document
                .getElementById("attemptId")
                .value
        );

    try {

        const response =
            await fetch(
                "/student/exams/submit",
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body: JSON.stringify({
                        attemptId,
                        answers
                    })
                }
            );

        if (!response.ok) {
            throw new Error(
                "시험 제출에 실패했습니다."
            );
        }

        const result =
            await response.json();

        clearInterval(timerInterval);

        sessionStorage.removeItem(
            `examDeadline-${attemptId}`
        );

        location.href =
            `/student/exams/attempts/${result.attemptId}/result?from=submit`;

    } catch (error) {

        console.error(error);

        isSubmitting = false;

        if (submitButton) {

            submitButton.disabled = false;

            submitButton.textContent =
                "제출하기";
        }

        alert(
            "시험 제출 중 오류가 발생했습니다."
        );
    }
}