(() => {

    const ROUND_SIZE = 4;


    // =========================================
    // DOM
    // =========================================

    const attemptId =
        Number(
            document
                .getElementById("attemptId")
                .value
        );


    const wordCards =
        document.getElementById(
            "wordCards"
        );

    const meaningCards =
        document.getElementById(
            "meaningCards"
        );


    const roundDisplay =
        document.getElementById(
            "roundDisplay"
        );

    const scoreDisplay =
        document.getElementById(
            "scoreDisplay"
        );

    const comboDisplay =
        document.getElementById(
            "comboDisplay"
        );

    const matchDisplay =
        document.getElementById(
            "matchDisplay"
        );


    const progressBar =
        document.getElementById(
            "progressBar"
        );

    const progressText =
        document.getElementById(
            "progressText"
        );


    const feedback =
        document.getElementById(
            "feedback"
        );


    const startOverlay =
        document.getElementById(
            "startOverlay"
        );

    const roundOverlay =
        document.getElementById(
            "roundOverlay"
        );

    const completeOverlay =
        document.getElementById(
            "completeOverlay"
        );


    const startButton =
        document.getElementById(
            "startButton"
        );

    const nextRoundButton =
        document.getElementById(
            "nextRoundButton"
        );

    const submitButton =
        document.getElementById(
            "submitButton"
        );


    const finalScore =
        document.getElementById(
            "finalScore"
        );

    const finalCombo =
        document.getElementById(
            "finalCombo"
        );


    // =========================================
    // 게임 상태
    // =========================================

    const game = {

        pairs: [],

        currentRound: 0,

        roundPairs: [],

        selectedWord: null,

        selectedMeaning: null,

        score: 0,

        combo: 0,

        maxCombo: 0,

        matchedCount: 0,

        roundMatchedCount: 0,

        answers: [],

        checking: false

    };


    // =========================================
    // Shuffle
    // =========================================

    function shuffle(array) {

        const copied =
            [...array];


        for (
            let i =
                copied.length - 1;
            i > 0;
            i--
        ) {

            const j =
                Math.floor(
                    Math.random() *
                    (i + 1)
                );


            [
                copied[i],
                copied[j]
            ] =
                [
                    copied[j],
                    copied[i]
                ];
        }


        return copied;
    }


    // =========================================
    // 데이터 로드
    // =========================================

    async function loadGameData() {

        const response =
            await fetch(
                `/student/exams/word-game/matching/data?attemptId=${attemptId}`
            );


        if (!response.ok) {

            throw new Error(
                "게임 데이터를 불러오지 못했습니다."
            );
        }


        const result =
            await response.json();


        if (!result.success) {

            throw new Error(
                result.message ||
                "게임 데이터를 불러오지 못했습니다."
            );
        }


        game.pairs =
            result.pairs || [];


        if (
            game.pairs.length === 0
        ) {

            throw new Error(
                "게임에 사용할 단어가 없습니다."
            );
        }
    }


    // =========================================
    // 게임 시작
    // =========================================

    async function startGame() {

        startButton.disabled =
            true;

        startButton.textContent =
            "불러오는 중...";


        try {

            await loadGameData();


            startOverlay.classList.add(
                "hidden"
            );


            loadRound();

        } catch (error) {

            alert(
                error.message
            );


            startButton.disabled =
                false;

            startButton.textContent =
                "게임 시작";
        }
    }


    // =========================================
    // 현재 라운드
    // =========================================

    function loadRound() {

        clearSelections();


        wordCards.innerHTML =
            "";

        meaningCards.innerHTML =
            "";


        game.roundMatchedCount =
            0;


        const start =
            game.currentRound *
            ROUND_SIZE;

        const end =
            start +
            ROUND_SIZE;


        game.roundPairs =
            game.pairs.slice(
                start,
                end
            );


        const totalRounds =
            Math.ceil(
                game.pairs.length /
                ROUND_SIZE
            );


        roundDisplay.textContent =
            `${game.currentRound + 1} / ${totalRounds}`;


        const shuffledWords =
            shuffle(
                game.roundPairs
            );


        const shuffledMeanings =
            shuffle(
                game.roundPairs
            );


        shuffledWords.forEach(
            pair => {

                const button =
                    createWordCard(
                        pair
                    );

                wordCards.appendChild(
                    button
                );
            }
        );


        shuffledMeanings.forEach(
            pair => {

                const button =
                    createMeaningCard(
                        pair
                    );

                meaningCards.appendChild(
                    button
                );
            }
        );


        updateHud();
    }


    // =========================================
    // 영어 카드
    // =========================================

    function createWordCard(
        pair
    ) {

        const button =
            document.createElement(
                "button"
            );


        button.type =
            "button";

        button.className =
            "match-card word-card";


        button.dataset.questionId =
            pair.questionId;


        button.innerHTML = `
            <span class="card-small-label">
                WORD
            </span>

            <strong>
                ${escapeHtml(pair.word)}
            </strong>
        `;


        button.addEventListener(
            "click",
            () => {

                selectWord(
                    button,
                    pair
                );
            }
        );


        return button;
    }


    // =========================================
    // 뜻 카드
    // =========================================

    function createMeaningCard(
        pair
    ) {

        const button =
            document.createElement(
                "button"
            );


        button.type =
            "button";

        button.className =
            "match-card meaning-card";


        button.dataset.questionId =
            pair.questionId;


        button.innerHTML = `
            <span class="card-small-label">
                MEANING
            </span>

            <strong>
                ${escapeHtml(pair.answer)}
            </strong>
        `;


        button.addEventListener(
            "click",
            () => {

                selectMeaning(
                    button,
                    pair
                );
            }
        );


        return button;
    }


    // =========================================
    // 영어 선택
    // =========================================

    function selectWord(
        element,
        pair
    ) {

        if (
            game.checking ||
            element.classList.contains(
                "matched"
            )
        ) {
            return;
        }


        document
            .querySelectorAll(
                ".word-card.selected"
            )
            .forEach(
                card =>
                    card.classList.remove(
                        "selected"
                    )
            );


        element.classList.add(
            "selected"
        );


        game.selectedWord = {
            element,
            pair
        };


        tryMatch();
    }


    // =========================================
    // 뜻 선택
    // =========================================

    function selectMeaning(
        element,
        pair
    ) {

        if (
            game.checking ||
            element.classList.contains(
                "matched"
            )
        ) {
            return;
        }


        document
            .querySelectorAll(
                ".meaning-card.selected"
            )
            .forEach(
                card =>
                    card.classList.remove(
                        "selected"
                    )
            );


        element.classList.add(
            "selected"
        );


        game.selectedMeaning = {
            element,
            pair
        };


        tryMatch();
    }


    // =========================================
    // 매칭 판정
    // =========================================

    function tryMatch() {

        if (
            !game.selectedWord ||
            !game.selectedMeaning ||
            game.checking
        ) {
            return;
        }


        game.checking =
            true;


        const word =
            game.selectedWord;

        const meaning =
            game.selectedMeaning;


        const correct =
            Number(
                word.pair.questionId
            ) ===
            Number(
                meaning.pair.questionId
            );


        if (correct) {

            handleCorrect(
                word,
                meaning
            );

        } else {

            handleWrong(
                word,
                meaning
            );
        }
    }


    // =========================================
    // 정답
    // =========================================

    function handleCorrect(
        word,
        meaning
    ) {

        game.combo++;

        game.maxCombo =
            Math.max(
                game.maxCombo,
                game.combo
            );


        const comboBonus =
            Math.min(
                game.combo * 10,
                100
            );


        game.score +=
            100 +
            comboBonus;


        game.matchedCount++;

        game.roundMatchedCount++;


        word.element.classList.remove(
            "selected"
        );

        meaning.element.classList.remove(
            "selected"
        );


        word.element.classList.add(
            "correct"
        );

        meaning.element.classList.add(
            "correct"
        );


        game.answers.push({

            questionId:
            word.pair.questionId,

            studentAnswer:
            meaning.pair.answer

        });


        showFeedback(
            `✨ 정답! +${100 + comboBonus}`,
            true
        );


        updateHud();


        setTimeout(
            () => {

                word.element.classList.add(
                    "matched"
                );

                meaning.element.classList.add(
                    "matched"
                );


                clearSelections();


                game.checking =
                    false;


                checkRoundComplete();

            },
            450
        );
    }


    // =========================================
    // 오답
    // =========================================

    function handleWrong(
        word,
        meaning
    ) {

        game.combo =
            0;


        word.element.classList.add(
            "wrong"
        );

        meaning.element.classList.add(
            "wrong"
        );


        showFeedback(
            "❌ 다시 생각해보세요!",
            false
        );


        updateHud();


        setTimeout(
            () => {

                word.element.classList.remove(
                    "selected",
                    "wrong"
                );

                meaning.element.classList.remove(
                    "selected",
                    "wrong"
                );


                clearSelections();


                game.checking =
                    false;

            },
            600
        );
    }


    // =========================================
    // 선택 초기화
    // =========================================

    function clearSelections() {

        game.selectedWord =
            null;

        game.selectedMeaning =
            null;
    }


    // =========================================
    // 라운드 완료 체크
    // =========================================

    function checkRoundComplete() {

        if (
            game.roundMatchedCount <
            game.roundPairs.length
        ) {

            return;
        }


        const totalRounds =
            Math.ceil(
                game.pairs.length /
                ROUND_SIZE
            );


        if (
            game.currentRound + 1 >=
            totalRounds
        ) {

            setTimeout(
                completeGame,
                500
            );

            return;
        }


        setTimeout(
            () => {

                roundOverlay.classList.remove(
                    "hidden"
                );

            },
            400
        );
    }


    // =========================================
    // 다음 라운드
    // =========================================

    function nextRound() {

        roundOverlay.classList.add(
            "hidden"
        );


        game.currentRound++;


        loadRound();
    }


    // =========================================
    // 게임 완료
    // =========================================

    function completeGame() {

        finalScore.textContent =
            game.score;

        finalCombo.textContent =
            game.maxCombo;


        completeOverlay.classList.remove(
            "hidden"
        );
    }


    // =========================================
    // HUD
    // =========================================

    function updateHud() {

        scoreDisplay.textContent =
            game.score;

        comboDisplay.textContent =
            game.combo;


        matchDisplay.textContent =
            `${game.matchedCount} / ${game.pairs.length}`;


        const percentage =
            game.pairs.length === 0
                ? 0
                : Math.round(
                    game.matchedCount *
                    100 /
                    game.pairs.length
                );


        progressBar.style.width =
            `${percentage}%`;

        progressText.textContent =
            `${percentage}%`;
    }


    // =========================================
    // 피드백
    // =========================================

    function showFeedback(
        message,
        correct
    ) {

        feedback.textContent =
            message;


        feedback.className =
            `feedback ${
                correct
                    ? "success"
                    : "fail"
            }`;


        setTimeout(
            () => {

                feedback.classList.add(
                    "hidden"
                );

            },
            900
        );
    }


    // =========================================
    // 시험 제출
    // =========================================

    async function submitExam() {

        submitButton.disabled =
            true;

        submitButton.textContent =
            "저장 중...";


        try {

            const csrfToken =
                document
                    .querySelector(
                        'meta[name="_csrf"]'
                    )
                    ?.getAttribute(
                        "content"
                    );


            const csrfHeader =
                document
                    .querySelector(
                        'meta[name="_csrf_header"]'
                    )
                    ?.getAttribute(
                        "content"
                    );


            const headers = {
                "Content-Type":
                    "application/json"
            };


            if (
                csrfToken &&
                csrfHeader
            ) {

                headers[
                    csrfHeader
                    ] =
                    csrfToken;
            }


            const response =
                await fetch(
                    "/student/exams/submit",
                    {

                        method:
                            "POST",

                        headers,

                        body:
                            JSON.stringify({

                                attemptId,

                                answers:
                                game.answers

                            })
                    }
                );


            if (!response.ok) {

                throw new Error(
                    "시험 결과 저장에 실패했습니다."
                );
            }


            const result =
                await response.json();


            if (
                result.success === false
            ) {

                throw new Error(
                    result.message ||
                    "시험 결과 저장에 실패했습니다."
                );
            }


            location.href =
                "/student/exams";


        } catch (error) {

            alert(
                error.message
            );


            submitButton.disabled =
                false;

            submitButton.textContent =
                "시험 완료";
        }
    }


    // =========================================
    // XSS 보호
    // =========================================

    function escapeHtml(
        value
    ) {

        const div =
            document.createElement(
                "div"
            );

        div.textContent =
            value ?? "";

        return div.innerHTML;
    }


    // =========================================
    // Event
    // =========================================

    startButton.addEventListener(
        "click",
        startGame
    );


    nextRoundButton.addEventListener(
        "click",
        nextRound
    );


    submitButton.addEventListener(
        "click",
        submitExam
    );

})();