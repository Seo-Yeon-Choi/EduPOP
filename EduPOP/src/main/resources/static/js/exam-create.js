document.addEventListener("DOMContentLoaded", () => {
    loadCategories();

    const csrfToken =
        document.querySelector('meta[name="_csrf"]').content;

    const csrfHeader =
        document.querySelector('meta[name="_csrf_header"]').content;

    const tabs =
        document.querySelectorAll(".question-tab");

    const questionType =
        document.getElementById("questionType");

    const choiceSection =
        document.getElementById("choiceSection");

    const choiceList =
        document.getElementById("choiceList");

    const addChoiceButton =
        document.getElementById("addChoiceButton");

    const questions = [createEmptyQuestion(1)];

    let currentQuestionIndex = 0;

    const questionNumberList =
        document.getElementById("questionNumberList");

    const addQuestionButton =
        document.getElementById("addQuestionButton");

    const questionNumber =
        document.getElementById("questionNumber");

    const score =
        document.getElementById("score");

    const questionText =
        document.getElementById("questionText");

    const passage =
        document.getElementById("passage");

    const correctAnswer =
        document.getElementById("correctAnswer");

    const saveExamButton =
        document.getElementById("saveExamButton");

    const examType =
        document.getElementById("examType");

    const examMode =
        document.getElementById("examMode");

    const normalExamSection =
        document.getElementById("normalExamSection");

    const wordExamSection =
        document.getElementById("wordExamSection");

    const wordList =
        document.getElementById("wordList");

    const addWordButton =
        document.getElementById("addWordButton");

    const examTypeSelect =
        document.getElementById("examType");

    const largeCategory =
        document.getElementById('largeCategory');

    const smallCategory =
        document.getElementById('smallCategory');

    let categoryData = null;

    function isWordExamType(value) {
        const normalized = String(value ?? "")
            .replace(/\s+/g, "")
            .toUpperCase();

        return normalized === "WORD"
            || normalized === "단어시험"
            || normalized === "단어테스트";
    }

    async function loadCategories() {

        try {

            const response = await fetch('/exam/api/categories');

            if (!response.ok) {
                throw new Error('분류 목록 조회 실패');
            }

            categoryData = await response.json();

            renderExamTypes(categoryData.examTypes);
            renderLargeCategories(categoryData.largeCategories);
            renderSmallCategories(categoryData.smallCategories);

        } catch (error) {

            console.error('분류 조회 오류:', error);

        }
    }

    function renderExamTypes(examTypes) {

        const select = document.getElementById('examType');

        if (!select) {
            console.error('examType 요소를 찾을 수 없습니다.');
            return;
        }

        select.innerHTML =
            '<option value="">시험 종류를 선택하세요</option>';

        if (!examTypes) {
            return;
        }

        examTypes.forEach(category => {

            const option = document.createElement('option');

            option.value = category.categoryName;
            option.textContent = category.categoryName;

            select.appendChild(option);
        });
    }

    function renderLargeCategories(categories) {

        const select =
            document.getElementById('largeCategory');

        if (!select) {
            console.error('largeCategory 요소를 찾을 수 없습니다.');
            return;
        }

        select.innerHTML =
            '<option value="">대분류 선택</option>';

        if (!categories) {
            return;
        }

        categories.forEach(category => {

            const option =
                document.createElement('option');

            option.value = category.categoryName;
            option.textContent = category.categoryName;

            select.appendChild(option);
        });
    }

    function renderSmallCategories(categories) {

        const select =
            document.getElementById('smallCategory');

        if (!select) {
            console.error('smallCategory 요소를 찾을 수 없습니다.');
            return;
        }

        select.innerHTML =
            '<option value="">소분류 선택</option>';

        if (!categories) {
            return;
        }

        categories.forEach(category => {

            const option =
                document.createElement('option');

            option.value = category.categoryName;
            option.textContent = category.categoryName;

            select.appendChild(option);
        });
    }

    // ============================
    // 객관식 / 주관식
    // ============================

    tabs.forEach(tab => {

        tab.addEventListener("click", () => {

            tabs.forEach(t => t.classList.remove("active"));

            tab.classList.add("active");

            const type = tab.dataset.type;

            questionType.value = type;

            if (type === "MULTIPLE_CHOICE") {
                choiceSection.style.display = "block";
            } else {
                choiceSection.style.display = "none";
            }
        });
    });

    // ============================
    // 선지 추가
    // ============================

    addChoiceButton.addEventListener("click", () => {
            const count = choiceList.querySelectorAll(".choice-item").length;

            const div = document.createElement("div");

            div.className = "choice-item";

            div.innerHTML = `
                <span class="choice-number">${count + 1}</span>
                <input type="text" class="choice-input" placeholder="선지를 입력하세요.">
                <button type="button" class="remove-choice"> ×</button>
            `;
            choiceList.appendChild(div);
        }
    );

    // ============================
    // 선지 삭제
    // ============================

    choiceList.addEventListener("click", event => {
            if (!event.target.classList.contains("remove-choice")) {
                return;
            }

            const items = choiceList.querySelectorAll(".choice-item");

            if (items.length <= 2) {
                alert("객관식은 최소 2개의 선지가 필요합니다.");
                return;
            }

            event.target.closest(".choice-item").remove();

            updateChoiceNumbers();
        }
    );

    function updateChoiceNumbers() {
        const items = choiceList.querySelectorAll(".choice-item");

        items.forEach((item, index) => {
                item.querySelector(".choice-number").textContent = index + 1;
            }
        );
    }

    // ============================
    // PDF
    // ============================

    const pdfFile = document.getElementById("pdfFile");

    const pdfFileInfo = document.getElementById("pdfFileInfo");

    const pdfFileName = document.getElementById("pdfFileName");

    const removePdf = document.getElementById("removePdf");

    pdfFile.addEventListener("change", async () => {
            // =====================================
            // 1. 파일 선택 확인
            // =====================================

            if (!pdfFile.files || pdfFile.files.length === 0) {
                return;
            }

            const file = pdfFile.files[0];

            // =====================================
            // 2. PDF인지 확인
            // =====================================

            if (!file.name.toLowerCase().endsWith(".pdf")) {
                alert("PDF 파일만 선택할 수 있습니다.");

                pdfFile.value = "";

                return;
            }

            // =====================================
            // 3. 현재 시험 종류 가져오기
            // =====================================

            const examType = document.getElementById("examType").value;

            if (!examType || examType === "") {
                alert("시험 종류를 먼저 선택해주세요.");

                pdfFile.value = "";

                return;
            }

            // =====================================
            // 4. FormData 생성
            // =====================================

            const formData = new FormData();

            formData.append("file", file);

            formData.append("examType", examType);

            // =====================================
            // 5. 서버 요청
            // =====================================

            try {
                const response = await fetch("/teacher/exams/parse-pdf",
                    {
                        method: "POST",
                        headers: {
                            [csrfHeader]: csrfToken
                        },
                        body: formData });

                // =================================
                // 서버 오류
                // =================================

                if (!response.ok) {

                    const errorText = await response.text();

                    console.error("PDF 파싱 실패:", errorText);

                    alert("PDF 분석에 실패했습니다.");

                    return;
                }

                // =================================
                // 파싱 결과
                // =================================

                const parsedQuestions = await response.json();

                console.log("PDF 추출 문제:", parsedQuestions);

                // =================================
                // 시험 종류에 따라 UI 적용
                // =================================

                if (isWordExamType(examType)) {
                    applyParsedWordQuestions(parsedQuestions);
                } else {
                    applyParsedQuestions(parsedQuestions);
                }
            }
            catch (error) {
                console.error("PDF 처리 중 오류:", error);

                alert("PDF 처리 중 오류가 발생했습니다.");
            }
        }
    );

    function applyParsedQuestions(parsedQuestions) {
        questions.length = 0;

        parsedQuestions.forEach((question, index) => {
                questions.push({
                    questionNumber:
                        index + 1,
                    score:
                        question.score ?? 5,
                    largeCategory:
                        question.largeCategory ?? "",
                    smallCategory:
                        question.smallCategory ?? "",
                    questionType:
                        question.questionType
                        ?? "MULTIPLE_CHOICE",
                    questionText:
                        question.questionText ?? "",
                    passage:
                        question.passage ?? "",
                    correctAnswer:
                        question.correctAnswer ?? "",
                    choices:
                        question.choices ?? []
                });
            }
        );

        if (questions.length === 0) {
            alert("PDF에서 문제를 찾지 못했습니다.");
            return;
        }

        currentQuestionIndex = 0;

        loadQuestion(0);

        renderQuestionNumbers();
    }

    function applyParsedWordQuestions(parsedQuestions) {

        const wordList = document.getElementById("wordList");

        // =========================================
        // 기존 단어 입력 행 삭제
        // =========================================

        wordList.innerHTML = "";

        // =========================================
        // 결과가 없는 경우
        // =========================================

        if (!parsedQuestions || parsedQuestions.length === 0) {
            alert("PDF에서 단어를 찾지 못했습니다.");

            return;
        }

        // =========================================
        // 단어 / 뜻 입력 행 생성
        // =========================================

        parsedQuestions.forEach(question => {
                const row = document.createElement("div");

                row.className = "word-row";

                row.innerHTML = `
                <input type="text" class="word-input" placeholder="영단어" value="${escapeHtml(question.questionText ?? "")}">
                <input type="text" class="meaning-input" placeholder="뜻" value="${escapeHtml(question.correctAnswer ?? "")}">
                <button type="button" class="remove-word-button">삭제</button>
            `;
                wordList.appendChild(row);
            }
        );

        // =========================================
        // 추출 결과 안내
        // =========================================

        alert(`${parsedQuestions.length}개의 단어를 추출했습니다.`);
    }

    removePdf.addEventListener("click", () => {
            pdfFile.value = "";
            pdfFileName.textContent = "";
            pdfFileInfo.classList.add("hidden");
        }
    );

    examType.addEventListener("change", updateExamInputSection);

    // 페이지 처음 열렸을 때도 적용
    updateExamInputSection();

    addQuestionButton.addEventListener("click", () => {
        // 현재 작성 중인 문제 내용 저장
        saveCurrentQuestion();
        // 새로운 문제 번호
        const newQuestionNumber = questions.length + 1;
        // 새로운 빈 문제 추가
        const newQuestion = createEmptyQuestion(newQuestionNumber);
        questions.push(newQuestion);
        // 새로 만든 문제로 이동
        currentQuestionIndex = questions.length - 1;
        // 문제 번호 버튼 다시 그림
        renderQuestionNumbers();
        // 새 문제 화면 표시
        loadQuestion(currentQuestionIndex);
    });

    function saveCurrentQuestion() {
        if (currentQuestionIndex === null) {
            return;
        }
        questions[currentQuestionIndex] = {
            questionNumber:
                Number(questionNumber.value),
            score:
                Number(score.value),
            largeCategory:
                largeCategory ? largeCategory.value : "",
            smallCategory:
                smallCategory ? smallCategory.value : "",
            questionType:
            questionType.value,
            questionText:
            questionText.value,
            passage:
            passage.value,
            correctAnswer:
            correctAnswer.value,
            choices:
                getChoices()

        };
    }

    // =========================================
    // 문제 번호 버튼 생성
    // =========================================

    function renderQuestionNumbers() {
        questionNumberList.innerHTML = "";

        questions.forEach((question, index) => {

            const button = document.createElement("button");

            button.type = "button";

            button.className = "question-number-button";

            button.textContent = index + 1;

            // 현재 선택 중인 문제
            if (index === currentQuestionIndex) {
                button.classList.add("active");
            }

            button.addEventListener("click", () => {

                // 같은 문제를 누른 경우
                if (index === currentQuestionIndex) {
                    return;
                }

                // 이동 전에 현재 문제 저장
                saveCurrentQuestion();

                // 클릭한 문제로 이동
                currentQuestionIndex = index;

                loadQuestion(index);

                renderQuestionNumbers();

            });

            questionNumberList.appendChild(button);

        });

    }

    // =========================================
    // 현재 선지 가져오기
    // =========================================

    function getChoices() {
        const choiceInputs = document.querySelectorAll(".choice-input");

        const choices = [];

        choiceInputs.forEach(input => {

            const value = input.value.trim();

            if (value !== "") {choices.push({choiceText: value});

            }
        });

        return choices;

    }

    // =========================================
    // 단어 추가
    // =========================================

    addWordButton.addEventListener("click", () => {

        const row = document.createElement("div");

        row.className = "word-row";

        row.innerHTML = `
        <input type="text" class="word-input" placeholder="영단어">
        <input type="text" class="meaning-input" placeholder="뜻">
        <button type="button" class="remove-word-button">삭제</button>
    `;
        wordList.appendChild(row);
    });

    // =========================================
    // 단어 삭제
    // =========================================
    wordList.addEventListener("click", event => {

        if (event.target.classList.contains("remove-word-button")) {
            event.target.closest(".word-row").remove();
        }
    });

    // =========================================
    // 단어 데이터 -> ExamQuestion[]으로 변환
    // =========================================

    function buildWordQuestions() {

        const rows = document.querySelectorAll(".word-row");

        const wordQuestions = [];

        rows.forEach(row => {

            const word = row.querySelector(".word-input").value.trim();

            const meaning = row.querySelector(".meaning-input").value.trim();

            if (word === "" || meaning === "") {
                return;
            }

            wordQuestions.push({
                questionNumber:
                    wordQuestions.length + 1,
                questionType:
                    "SHORT_ANSWER",
                largeCategory:
                    "",
                smallCategory:
                    "",
                score:
                    1,
                questionText:
                word,
                passage:
                    "",
                correctAnswer:
                meaning,
                sortOrder:
                    wordQuestions.length + 1,
                choices:
                    []
            });
        });

        return wordQuestions;
    }

    // =========================================
    // 저장된 문제 불러오기
    // =========================================

    function loadQuestion(index) {

        const question = questions[index];

        questionNumber.value =
            question.questionNumber ?? index + 1;

        score.value =
            question.score ?? 5;

        if (largeCategory) {
            largeCategory.value = question.largeCategory ?? "";
        }

        if (smallCategory) {
            smallCategory.value = question.smallCategory ?? "";
        }

        questionText.value =
            question.questionText ?? "";

        passage.value =
            question.passage ?? "";

        correctAnswer.value =
            question.correctAnswer ?? "";

        setQuestionType(question.questionType ?? "MULTIPLE_CHOICE");

        renderChoices(question.choices ?? []);

        renderQuestionNumbers();

    }

    // =========================================
    // 문제 유형 변경
    // =========================================

    function setQuestionType(type) {

        questionType.value = type;

        document.querySelectorAll(".question-tab").forEach(tab => {

            tab.classList.remove("active");

            if (tab.dataset.type === type) {
                tab.classList.add("active");
            }
        });

        const choiceSection = document.getElementById("choiceSection");

        if (type === "MULTIPLE_CHOICE") {
            choiceSection.style.display = "block";
        }
        else {
            choiceSection.style.display = "none";
        }
    }

    // =========================================
    // 선지 다시 출력
    // =========================================

    function renderChoices(choices) {

        choiceList.innerHTML = "";

        // 저장된 선지가 없는 경우
        if (!choices || choices.length === 0) {
            for (let i = 0; i < 4; i++) {
                createChoiceInput("");
            }
            return;
        }

        choices.forEach(choice => {
            createChoiceInput(choice.choiceText);
        });
    }

    // =========================================
    // 선지 입력칸 생성
    // =========================================

    function createChoiceInput(value) {

        const count = choiceList.querySelectorAll(".choice-item").length;

        const div = document.createElement("div");

        div.className = "choice-item";

        div.innerHTML = `
            <span class="choice-number"> ${count + 1} </span>
            <input type="text" class="choice-input" placeholder="선지를 입력하세요." value="${escapeHtml(value)}">
            <button type="button" class="remove-choice">×</button>
        `;

        choiceList.appendChild(div);
    }

    function updateExamInputSection() {
        const examTypeValue = examType.value;

        console.log("시험 종류 변경:", examTypeValue);

        // =========================================
        // 단어 시험
        // =========================================

        if (isWordExamType(examTypeValue)) {
            normalExamSection.style.display = "none";

            wordExamSection.style.display = "block";

            // 단어시험은 온라인 기본
            examMode.value = "ONLINE";
        }

            // =========================================
            // 일반 시험
        // =========================================

        else {
            normalExamSection.style.display = "block";

            wordExamSection.style.display = "none";
        }
    }

    // =========================================
    // 선택된 문제 번호 표시
    // =========================================

    function updateActiveQuestionButton() {

        const buttons = document.querySelectorAll(".question-number-button");

        buttons.forEach((button, index) => {

            button.classList.remove("active");

            if (currentQuestionIndex === index) {
                button.classList.add("active");
            }
        });
    }

    // =========================================
    // 빈 문제를 만들어주는 함수
    // =========================================
    function createEmptyQuestion(number) {
        return {
            questionNumber: number,
            score: 5,
            largeCategory: "",
            smallCategory: "",
            questionType: "MULTIPLE_CHOICE",
            questionText: "",
            passage: "",
            correctAnswer: "",
            choices: [
                { choiceText: "" },
                { choiceText: "" },
                { choiceText: "" },
                { choiceText: "" }
            ]
        };
    }

    // =========================================
    // HTML 특수문자 처리
    // =========================================

    function escapeHtml(value) {
        if (value === null || value === undefined) {
            return "";
        }

        return String(value)

            .replaceAll("&", "&amp;")

            .replaceAll("\"", "&quot;")

            .replaceAll("'", "&#039;")

            .replaceAll("<", "&lt;")

            .replaceAll(">", "&gt;");
    }
    // 시험지 저장

    saveExamButton.addEventListener("click", async () => {

            // =====================================
            // 1. 반 선택
            // =====================================

            const classId = document.getElementById("classId").value;

            console.log("등록 버튼 클릭 시 classId =", classId);

            if (classId === "") {
                alert("시험을 등록할 반을 선택해주세요.");

                return;
            }

            // =====================================
            // 2. 기본 정보
            // =====================================

            const examTitle = document.getElementById("examTitle").value.trim();

            const examType = document.getElementById("examType").value;

            const examMode = document.getElementById("examMode").value;

            const examDate = document.getElementById("examDate").value;

            // =====================================
            // 3. 제목 검사
            // =====================================

            if (examTitle === "") {

                alert("시험명을 입력해주세요.");

                return;
            }

            if (examType === "") {
                alert("시험 종류를 선택해주세요.");
                return;
            }

            // =====================================
            // 4. 문제 배열 생성
            // =====================================

            let examQuestions = [];

            // =====================================
            // 단어 시험
            // =====================================

            if (isWordExamType(examType)) {

                examQuestions = buildWordQuestions();

                if (examQuestions.length === 0) {
                    alert("단어를 하나 이상 입력해주세요.");

                    return;
                }

                // ---------------------------------
                // 단어 데이터 검증
                // ---------------------------------

                for (let i = 0; i < examQuestions.length; i++) {

                    const question = examQuestions[i];

                    // 단어
                    if (!question.questionText || question.questionText.trim() === "") {

                        alert(`${i + 1}번째 단어를 입력해주세요.`);

                        return;
                    }

                    // 뜻
                    if (!question.correctAnswer || question.correctAnswer.trim() === "") {

                        alert(`${i + 1}번째 단어의 뜻을 입력해주세요.`);

                        return;
                    }
                }
            }

                // =====================================
                // 일반 시험
            // =====================================

            else {
                // 현재 문제 저장
                saveCurrentQuestion();

                if (questions.length === 0) {
                    alert("문제를 하나 이상 입력해주세요.");

                    return;
                }

                // ---------------------------------
                // 문제 검증
                // ---------------------------------

                for (let i = 0; i < questions.length; i++) {
                    const question = questions[i];

                    // 문제 내용
                    if (!question.questionText || question.questionText.trim() === "") {
                        alert(`${i + 1}번 문제 내용을 입력해주세요.`);

                        return;
                    }

                    // 대분류
                    if (!question.largeCategory || question.largeCategory.trim() === "") {
                        alert(`${i + 1}번 문제의 대분류를 선택해주세요.`);
                        return;
                    }

                    // 소분류
                    if (!question.smallCategory || question.smallCategory.trim() === "") {
                        alert(`${i + 1}번 문제의 소분류를 선택해주세요.`);
                        return;
                    }

                    // -----------------------------
                    // 객관식 선지 검사
                    // -----------------------------

                    if (question.questionType === "MULTIPLE_CHOICE") {

                        if (!question.choices || question.choices.length < 2) {

                            alert(`${i + 1}번 객관식 문제는 선지를 최소 2개 입력해주세요.`);

                            return;
                        }

                        const hasEmptyChoice = question.choices.some(
                            choice => !choice.choiceText || choice.choiceText.trim() === "");

                        if (hasEmptyChoice) {

                            alert(`${i + 1}번 문제의 빈 선지를 입력하거나 삭제해주세요.`);

                            return;
                        }
                    }
                }

                examQuestions = questions;
            }

            // =====================================
            // 5. Exam 객체
            // =====================================

            const exam = {

                classId:
                    Number(classId),

                title:
                examTitle,

                examType:
                examType,

                // 단어시험은 온라인
                examMode:
                    isWordExamType(examType)
                        ? "ONLINE"
                        : examMode,

                examDate:
                    examDate === ""
                        ? null
                        : examDate,

                questions:
                examQuestions
            };

            console.log("시험지 등록 데이터:", exam);

            // =====================================
            // 6. 서버 저장
            // =====================================

            try {

                const response = await fetch("/teacher/exams",
                    {
                        method:
                            "POST",
                        headers: {
                            "Content-Type": "application/json",
                            [csrfHeader]: csrfToken
                        },
                        body: JSON.stringify(exam)
                    }
                );

                // =================================
                // 오류
                // =================================

                if (!response.ok) {

                    const errorText = await response.text();

                    console.error("시험지 등록 실패:", errorText);

                    alert("시험지 등록에 실패했습니다.");

                    return;
                }

                // =================================
                // examId
                // =================================

                const examId = await response.json();

                console.log("생성된 examId:", examId);

                // =================================
                // 성공
                // =================================

                alert("시험지가 등록되었습니다.");

                window.location.href = "/teacher/exams";
            }

            catch (error) {

                console.error("시험지 등록 중 오류:", error);

                alert("시험지를 등록하는 중 오류가 발생했습니다.");
            }
        }
    );

    function updateExamTypeDisplay() {
        const isWordExam = isWordExamType(examTypeSelect.value);

        normalExamSection.style.display = isWordExam ? "none" : "block";
        wordExamSection.style.display = isWordExam ? "block" : "none";

        // 단어 시험이면 문제 추가 버튼 숨김
        addQuestionButton.style.display = isWordExam ? "none" : "inline-flex";
    }

    examTypeSelect.addEventListener("change", updateExamTypeDisplay);

    // 화면을 처음 열었을 때도 현재 선택값을 적용
    updateExamTypeDisplay();

});