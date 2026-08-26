document.addEventListener("DOMContentLoaded", () => {
    const editPage = document.getElementById("examEditPage");
    const saveButtons = [
        document.getElementById("saveExamButton"),
        document.getElementById("saveExamBottomButton")
    ];

    if (!editPage) {
        return;
    }

    saveButtons.forEach(button => button.addEventListener("click", saveExam));

    async function saveExam() {
        const examId = Number(editPage.dataset.examId);
        const title = document.getElementById("examTitle").value.trim();
        const classId = Number(document.getElementById("classId").value);

        if (!title) {
            alert("시험명을 입력해주세요.");
            return;
        }

        if (!classId) {
            alert("대상 반을 선택해주세요.");
            return;
        }

        const questions = [];
        const questionCards = document.querySelectorAll(".question-edit-card");

        for (const card of questionCards) {
            const questionText = card.querySelector(".question-text-input").value.trim();

            if (!questionText) {
                alert("모든 문제 내용을 입력해주세요.");
                return;
            }

            const choices = [];
            for (const input of card.querySelectorAll(".choice-input")) {
                const choiceText = input.value.trim();

                if (!choiceText) {
                    alert("선지 내용은 비워 둘 수 없습니다.");
                    return;
                }

                choices.push({
                    choiceId: Number(input.dataset.choiceId),
                    choiceText
                });
            }

            const scoreValue = card.querySelector(".question-score").value;
            questions.push({
                questionId: Number(card.dataset.questionId),
                score: scoreValue === "" ? null : Number(scoreValue),
                questionTypeTag: card.querySelector(".question-type-tag").value.trim(),
                questionText,
                passage: card.querySelector(".question-passage-input").value.trim(),
                correctAnswer: card.querySelector(".correct-answer-input").value.trim(),
                choices
            });
        }

        const exam = {
            examId,
            classId,
            title,
            examType: document.getElementById("examType").value,
            examMode: document.getElementById("examMode").value,
            status: document.getElementById("examStatus").value,
            examDate: document.getElementById("examDate").value || null,
            questions
        };

        setSaving(true);

        try {
            const response = await fetch(`/teacher/exams/${examId}`, {
                method: "PUT",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(exam)
            });

            if (!response.ok) {
                const message = await response.text();
                console.error("시험지 수정 실패:", message);
                alert("시험지 수정에 실패했습니다. 입력 내용을 확인해주세요.");
                return;
            }

            alert("시험지가 수정되었습니다.");
            window.location.href = `/teacher/exams/${examId}`;
        } catch (error) {
            console.error("시험지 수정 중 오류:", error);
            alert("시험지를 수정하는 중 오류가 발생했습니다.");
        } finally {
            setSaving(false);
        }
    }

    function setSaving(isSaving) {
        saveButtons.forEach(button => {
            button.disabled = isSaving;
            button.textContent = isSaving ? "저장 중..." : "변경사항 저장";
        });
    }
});
