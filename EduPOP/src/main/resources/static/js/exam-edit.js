document.addEventListener("DOMContentLoaded", () => {
    const csrfToken =
        document.querySelector('meta[name="_csrf"]').content;

    const csrfHeader =
        document.querySelector('meta[name="_csrf_header"]').content;


    const editPage =
        document.getElementById("examEditPage");

    const saveButtons = [
        document.getElementById("saveExamButton"),
        document.getElementById("saveExamBottomButton")
    ];

    if (!editPage) {
        return;
    }


    // =========================================
    // 분류 데이터
    // =========================================

    let categoryData = null;


    // =========================================
    // 페이지 초기화
    // =========================================

    init();


    async function init() {

        await loadCategories();

        saveButtons.forEach(button => {

            if (button) {
                button.addEventListener(
                    "click",
                    saveExam
                );
            }

        });
    }


    // =========================================
    // 시험 / 문항 분류 목록 조회
    // =========================================

    async function loadCategories() {

        try {

            const response =
                await fetch("/exam/api/categories");

            if (!response.ok) {

                throw new Error(
                    "분류 목록 조회 실패"
                );
            }

            categoryData =
                await response.json();

            console.log(
                "수정 페이지 분류 목록:",
                categoryData
            );


            // 시험 종류
            renderExamTypes(
                categoryData.examTypes ?? []
            );


            // 대분류
            renderLargeCategories(
                categoryData.largeCategories ?? []
            );


            // 소분류
            renderSmallCategories(
                categoryData.smallCategories ?? []
            );

        } catch (error) {

            console.error(
                "분류 목록을 불러오는 중 오류:",
                error
            );

            alert(
                "시험 분류 목록을 불러오지 못했습니다."
            );
        }
    }


    // =========================================
    // category 객체 → 이름
    // =========================================

    function getCategoryName(category) {

        if (typeof category === "string") {
            return category;
        }

        return category?.categoryName ?? "";
    }


    // =========================================
    // 공통 select 생성
    // =========================================

    function populateCategorySelect(
        select,
        categories,
        currentValue,
        placeholder
    ) {

        if (!select) {
            return;
        }

        select.innerHTML = "";

        // 기본 option
        const placeholderOption =
            document.createElement("option");

        placeholderOption.value = "";

        placeholderOption.textContent =
            placeholder;

        select.appendChild(
            placeholderOption
        );


        // API에서 받은 분류 추가
        categories.forEach(category => {

            const categoryName =
                getCategoryName(category);

            if (!categoryName) {
                return;
            }

            const option =
                document.createElement("option");

            option.value =
                categoryName;

            option.textContent =
                categoryName;

            select.appendChild(option);
        });


        // -------------------------------------
        // 기존 값이 master category에서
        // 삭제되었더라도 시험 수정 시 값이
        // 사라지지 않도록 기존값을 추가한다.
        // -------------------------------------

        if (
            currentValue &&
            !Array.from(select.options)
                .some(
                    option =>
                        option.value === currentValue
                )
        ) {

            const oldOption =
                document.createElement("option");

            oldOption.value =
                currentValue;

            oldOption.textContent =
                `${currentValue} (기존 값)`;

            select.appendChild(
                oldOption
            );
        }


        // 기존 시험 값 선택
        select.value =
            currentValue || "";
    }


    // =========================================
    // 시험 종류
    // =========================================

    function renderExamTypes(examTypes) {

        const examTypeSelect =
            document.getElementById(
                "examType"
            );

        const currentValue =
            examTypeSelect.dataset.currentValue
            ?? "";

        populateCategorySelect(
            examTypeSelect,
            examTypes,
            currentValue,
            "시험 종류를 선택하세요"
        );
    }


    // =========================================
    // 대분류
    // =========================================

    function renderLargeCategories(
        largeCategories
    ) {

        const selects =
            document.querySelectorAll(
                ".question-large-category"
            );

        selects.forEach(select => {

            const currentValue =
                select.dataset.currentValue
                ?? "";

            populateCategorySelect(
                select,
                largeCategories,
                currentValue,
                "대분류를 선택하세요"
            );

        });
    }


    // =========================================
    // 소분류
    // =========================================

    function renderSmallCategories(
        smallCategories
    ) {

        const selects =
            document.querySelectorAll(
                ".question-small-category"
            );

        selects.forEach(select => {

            const currentValue =
                select.dataset.currentValue
                ?? "";

            populateCategorySelect(
                select,
                smallCategories,
                currentValue,
                "소분류를 선택하세요"
            );

        });
    }


    // =========================================
    // 시험 저장
    // =========================================

    async function saveExam() {

        const examId =
            Number(
                editPage.dataset.examId
            );

        const title =
            document
                .getElementById("examTitle")
                .value
                .trim();

        const classId =
            Number(
                document
                    .getElementById("classId")
                    .value
            );

        const examType =
            document
                .getElementById("examType")
                .value;


        // =====================================
        // 기본 검증
        // =====================================

        if (!title) {

            alert(
                "시험명을 입력해주세요."
            );

            return;
        }


        if (!classId) {

            alert(
                "대상 반을 선택해주세요."
            );

            return;
        }


        if (!examType) {

            alert(
                "시험 종류를 선택해주세요."
            );

            return;
        }


        // =====================================
        // 문항 데이터 생성
        // =====================================

        const questions = [];

        const questionCards =
            document.querySelectorAll(
                ".question-edit-card"
            );


        for (const card of questionCards) {

            const questionText =
                card
                    .querySelector(
                        ".question-text-input"
                    )
                    .value
                    .trim();

            const largeCategory =
                card
                    .querySelector(
                        ".question-large-category"
                    )
                    .value;

            const smallCategory =
                card
                    .querySelector(
                        ".question-small-category"
                    )
                    .value;


            // 문제
            if (!questionText) {

                alert(
                    "모든 문제 내용을 입력해주세요."
                );

                return;
            }


            // 대분류
            if (!largeCategory) {

                alert(
                    "모든 문항의 대분류를 선택해주세요."
                );

                return;
            }


            // 소분류
            if (!smallCategory) {

                alert(
                    "모든 문항의 소분류를 선택해주세요."
                );

                return;
            }


            // =================================
            // 선지
            // =================================

            const choices = [];

            for (
                const input of
                card.querySelectorAll(
                    ".choice-input"
                )
                ) {

                const choiceText =
                    input.value.trim();

                if (!choiceText) {

                    alert(
                        "선지 내용은 비워 둘 수 없습니다."
                    );

                    return;
                }


                choices.push({

                    choiceId:
                        Number(
                            input.dataset.choiceId
                        ),

                    choiceText
                });
            }


            const scoreValue =
                card
                    .querySelector(
                        ".question-score"
                    )
                    .value;


            // =================================
            // 문항 객체
            // =================================

            questions.push({

                questionId:
                    Number(
                        card.dataset.questionId
                    ),

                score:
                    scoreValue === ""
                        ? null
                        : Number(scoreValue),

                largeCategory,

                smallCategory,

                questionText,

                passage:
                    card
                        .querySelector(
                            ".question-passage-input"
                        )
                        .value
                        .trim(),

                correctAnswer:
                    card
                        .querySelector(
                            ".correct-answer-input"
                        )
                        .value
                        .trim(),

                choices

            });
        }


        // =====================================
        // 시험 객체
        // =====================================

        const exam = {

            examId,

            classId,

            title,

            examType,

            examMode:
            document
                .getElementById("examMode")
                .value,

            status:
            document
                .getElementById("examStatus")
                .value,

            examDate:
                document
                    .getElementById("examDate")
                    .value || null,

            questions

        };


        console.log(
            "시험지 수정 요청:",
            exam
        );


        // =====================================
        // 서버 요청
        // =====================================

        setSaving(true);

        try {

            const response =
                await fetch(
                    `/teacher/exams/${examId}`,
                    {
                        method: "PUT",

                        headers: {
                            "Content-Type":
                                "application/json",
                            [csrfHeader]: csrfToken
                        },

                        body:
                            JSON.stringify(exam)
                    }
                );


            if (!response.ok) {

                const message =
                    await response.text();

                console.error(
                    "시험지 수정 실패:",
                    message
                );

                alert(
                    message ||
                    "시험지 수정에 실패했습니다."
                );

                return;
            }


            alert(
                "시험지가 수정되었습니다."
            );


            window.location.href =
                `/teacher/exams/${examId}`;

        } catch (error) {

            console.error(
                "시험지 수정 중 오류:",
                error
            );

            alert(
                "시험지를 수정하는 중 오류가 발생했습니다."
            );

        } finally {

            setSaving(false);
        }
    }


    // =========================================
    // 저장 버튼 상태
    // =========================================

    function setSaving(isSaving) {

        saveButtons.forEach(button => {

            if (!button) {
                return;
            }

            button.disabled =
                isSaving;

            button.textContent =
                isSaving
                    ? "저장 중..."
                    : "변경사항 저장";
        });
    }

});