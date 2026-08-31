(() => {

    // ==============================================
    // DOM
    // ==============================================

    const attemptId =
        Number(
            document
                .getElementById("attemptId")
                .value
        );


    const questionElements =
        document.querySelectorAll(
            ".question-data"
        );


    const questionText =
        document.getElementById(
            "questionText"
        );


    const player =
        document.getElementById(
            "mazePlayer"
        );


    const mazeField =
        document.getElementById(
            "mazeField"
        );


    const mazeMessage =
        document.getElementById(
            "mazeMessage"
        );


    const blockedEffect =
        document.getElementById(
            "blockedEffect"
        );


    const lifeDisplay =
        document.getElementById(
            "lifeDisplay"
        );

    const scoreDisplay =
        document.getElementById(
            "scoreDisplay"
        );

    const comboDisplay =
        document.getElementById(
            "comboDisplay"
        );

    const roomDisplay =
        document.getElementById(
            "roomDisplay"
        );


    const progressBar =
        document.getElementById(
            "progressBar"
        );

    const progressText =
        document.getElementById(
            "progressText"
        );


    const startOverlay =
        document.getElementById(
            "startOverlay"
        );

    const completeOverlay =
        document.getElementById(
            "completeOverlay"
        );


    const startButton =
        document.getElementById(
            "startButton"
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

    const finalRooms =
        document.getElementById(
            "finalRooms"
        );


    const pathButtons = [

        document.getElementById(
            "pathTop"
        ),

        document.getElementById(
            "pathRight"
        ),

        document.getElementById(
            "pathBottom"
        ),

        document.getElementById(
            "pathLeft"
        )

    ];


    // ==============================================
    // STATE
    // ==============================================

    const game = {

        questions: [],

        currentIndex: 0,

        life: 3,

        score: 0,

        combo: 0,

        maxCombo: 0,

        correctCount: 0,

        answers: [],

        options: [],

        checking: false,

        playerPosition: {
            x: 50,
            y: 68
        }

    };


    // ==============================================
    // 문제 읽기
    // ==============================================

    questionElements.forEach(
        element => {

            game.questions.push({

                questionId:
                    Number(
                        element.dataset.questionId
                    ),

                text:
                element.dataset.questionText

            });

        }
    );


    // ==============================================
    // Shuffle
    // ==============================================

    function shuffle(
        array
    ) {

        const copy =
            [...array];


        for (
            let i =
                copy.length - 1;
            i > 0;
            i--
        ) {

            const j =
                Math.floor(
                    Math.random() *
                    (i + 1)
                );


            [
                copy[i],
                copy[j]
            ] = [
                copy[j],
                copy[i]
            ];

        }


        return copy;
    }


    // ==============================================
    // 시작
    // ==============================================

    function startGame() {

        if (
            game.questions.length === 0
        ) {

            alert(
                "게임 문제를 찾을 수 없습니다."
            );

            return;
        }


        startOverlay.classList.add(
            "hidden"
        );


        loadRoom();
    }


    // ==============================================
    // 방 로드
    // ==============================================

    async function loadRoom() {

        if (
            game.currentIndex >=
            game.questions.length
        ) {

            completeGame();

            return;
        }


        game.checking =
            true;


        resetPathButtons();


        resetPlayerPosition();


        const question =
            game.questions[
                game.currentIndex
                ];


        questionText.textContent =
            question.text;


        roomDisplay.textContent =
            `${
                game.currentIndex + 1
            } / ${
                game.questions.length
            }`;


        mazeMessage.textContent =
            "정답 길을 찾아보세요!";


        updateHud();


        try {

            const response =
                await fetch(

                    "/student/exams/word-game-take/round"
                    +
                    `?attemptId=${attemptId}`
                    +
                    `&questionId=${question.questionId}`

                );


            if (!response.ok) {

                throw new Error(
                    "선택지를 불러오지 못했습니다."
                );

            }


            const result =
                await response.json();


            if (!result.success) {

                throw new Error(
                    result.message ||
                    "선택지를 불러오지 못했습니다."
                );

            }


            game.options =
                shuffle(
                    result.options
                );


            renderOptions();


            game.checking =
                false;


        } catch (error) {

            alert(
                error.message
            );

        }
    }


    // ==============================================
    // 보기 표시
    // ==============================================

    function renderOptions() {

        pathButtons.forEach(
            (
                button,
                index
            ) => {

                const option =
                    game.options[
                        index
                        ];


                if (
                    option === undefined
                ) {

                    button.classList.add(
                        "hidden"
                    );

                    return;
                }


                button.classList.remove(
                    "hidden"
                );


                button.dataset.answer =
                    option;


                const strong =
                    button.querySelector(
                        "strong"
                    );


                strong.textContent =
                    option;

            }
        );
    }


    // ==============================================
    // 길 선택
    // ==============================================

    async function choosePath(
        button,
        index
    ) {

        if (
            game.checking
        ) {

            return;
        }


        const answer =
            button.dataset.answer;


        if (!answer) {
            return;
        }


        game.checking =
            true;


        const question =
            game.questions[
                game.currentIndex
                ];


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

                    "/student/exams/word-game-take/check",

                    {

                        method:
                            "POST",

                        headers,

                        body:
                            JSON.stringify({

                                attemptId,

                                questionId:
                                question.questionId,

                                studentAnswer:
                                answer

                            })

                    }

                );


            if (!response.ok) {

                throw new Error(
                    "정답 판정에 실패했습니다."
                );

            }


            const result =
                await response.json();


            if (
                result.correct
            ) {

                handleCorrect(
                    button,
                    index,
                    answer
                );

            } else {

                handleWrong(
                    button,
                    index
                );

            }


        } catch (error) {

            alert(
                error.message
            );


            game.checking =
                false;
        }
    }


    // ==============================================
    // 정답
    // ==============================================

    function handleCorrect(
        button,
        directionIndex,
        answer
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


        game.correctCount++;


        const question =
            game.questions[
                game.currentIndex
                ];


        game.answers.push({

            questionId:
            question.questionId,

            studentAnswer:
            answer

        });


        button.classList.add(
            "correct-path"
        );


        mazeMessage.textContent =
            `✨ 정답! 길이 열렸습니다. +${
                100 + comboBonus
            }`;


        movePlayer(
            directionIndex,
            true
        );


        updateHud();


        setTimeout(
            () => {

                game.currentIndex++;

                loadRoom();

            },
            850
        );
    }


    // ==============================================
    // 오답
    // ==============================================

    function handleWrong(
        button,
        directionIndex
    ) {

        game.combo =
            0;


        game.life =
            Math.max(
                0,
                game.life - 1
            );


        button.classList.add(
            "wrong-path"
        );


        mazeMessage.textContent =
            "💥 막힌 길입니다! 다른 길을 찾아보세요.";


        movePlayer(
            directionIndex,
            false
        );


        showBlockedEffect();


        updateHud();


        setTimeout(
            () => {

                button.classList.remove(
                    "wrong-path"
                );


                resetPlayerPosition();


                if (
                    game.life <= 0
                ) {

                    game.life =
                        3;

                    mazeMessage.textContent =
                        "❤️ 생명이 회복되었습니다. 다시 도전하세요!";

                    updateHud();
                }


                game.checking =
                    false;

            },
            700
        );
    }


    // ==============================================
    // 플레이어 이동
    // ==============================================

    function movePlayer(
        directionIndex,
        correct
    ) {

        const positions = [

            {
                x: 50,
                y: 32
            },

            {
                x: 76,
                y: 50
            },

            {
                x: 50,
                y: 77
            },

            {
                x: 24,
                y: 50
            }

        ];


        const position =
            positions[
                directionIndex
                ];


        player.style.left =
            `${position.x}%`;

        player.style.top =
            `${position.y}%`;


        if (
            correct
        ) {

            player.classList.add(
                "success-move"
            );

        } else {

            player.classList.add(
                "failed-move"
            );

        }


        setTimeout(
            () => {

                player.classList.remove(
                    "success-move",
                    "failed-move"
                );

            },
            600
        );
    }


    // ==============================================
    // 시작 위치
    // ==============================================

    function resetPlayerPosition() {

        player.style.left =
            "50%";

        player.style.top =
            "68%";

    }


    // ==============================================
    // 막힌 길
    // ==============================================

    function showBlockedEffect() {

        blockedEffect.classList.remove(
            "hidden"
        );


        blockedEffect.classList.remove(
            "animate"
        );


        void blockedEffect.offsetWidth;


        blockedEffect.classList.add(
            "animate"
        );


        setTimeout(
            () => {

                blockedEffect.classList.add(
                    "hidden"
                );

            },
            650
        );
    }


    // ==============================================
    // 버튼 초기화
    // ==============================================

    function resetPathButtons() {

        pathButtons.forEach(
            button => {

                button.classList.remove(
                    "correct-path",
                    "wrong-path",
                    "hidden"
                );


                button.dataset.answer =
                    "";


                const strong =
                    button.querySelector(
                        "strong"
                    );


                strong.textContent =
                    "?";

            }
        );
    }


    // ==============================================
    // HUD
    // ==============================================

    function updateHud() {

        lifeDisplay.textContent =
            "❤️".repeat(
                Math.max(
                    game.life,
                    0
                )
            )
            ||
            "💔";


        scoreDisplay.textContent =
            game.score;


        comboDisplay.textContent =
            game.combo;


        const percentage =
            game.questions.length === 0
                ? 0
                : Math.round(
                    game.currentIndex *
                    100 /
                    game.questions.length
                );


        progressBar.style.width =
            `${percentage}%`;


        progressText.textContent =
            `${percentage}%`;

    }


    // ==============================================
    // 완료
    // ==============================================

    function completeGame() {

        progressBar.style.width =
            "100%";


        progressText.textContent =
            "100%";


        player.style.left =
            "50%";

        player.style.top =
            "14%";


        mazeMessage.textContent =
            "🚪 출구를 찾았습니다!";


        finalScore.textContent =
            game.score;


        finalCombo.textContent =
            game.maxCombo;


        finalRooms.textContent =
            `${
                game.correctCount
            } / ${
                game.questions.length
            }`;


        setTimeout(
            () => {

                completeOverlay.classList.remove(
                    "hidden"
                );

            },
            800
        );
    }


    // ==============================================
    // 제출
    // ==============================================

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


    // ==============================================
    // Event
    // ==============================================

    pathButtons.forEach(
        (
            button,
            index
        ) => {

            button.addEventListener(
                "click",
                () => {

                    choosePath(
                        button,
                        index
                    );

                }
            );

        }
    );


    startButton.addEventListener(
        "click",
        startGame
    );


    submitButton.addEventListener(
        "click",
        submitExam
    );

})();