document.addEventListener(
    "DOMContentLoaded",
    () => {

        const game = {

            questions: [],

            currentQuestionIndex: 0,

            playerX: 50,

            playerSpeed: 1.15,

            leftPressed: false,

            rightPressed: false,

            score: 0,

            combo: 0,

            maxCombo: 0,

            hearts: 3,

            correctCount: 0,

            answers: [],

            objects: [],

            gameRunning: false,

            roundRunning: false,

            frameId: null,

            lastFrameTime: null,

            spawnTimer: null,

            messageTimer: null
        };


        const attemptId =
            Number(
                document
                    .getElementById(
                        "attemptId"
                    )
                    .value
            );


        const field =
            document.getElementById(
                "gameField"
            );


        const player =
            document.getElementById(
                "player"
            );


        const objectLayer =
            document.getElementById(
                "fallingObjectLayer"
            );


        const targetWord =
            document.getElementById(
                "targetWord"
            );


        const scoreDisplay =
            document.getElementById(
                "scoreDisplay"
            );


        const comboDisplay =
            document.getElementById(
                "comboDisplay"
            );


        const lifeDisplay =
            document.getElementById(
                "lifeDisplay"
            );


        const currentDisplay =
            document.getElementById(
                "currentQuestionDisplay"
            );


        const totalDisplay =
            document.getElementById(
                "totalQuestionDisplay"
            );


        const progress =
            document.getElementById(
                "gameProgress"
            );


        const gameMessage =
            document.getElementById(
                "gameMessage"
            );


        const comboPopup =
            document.getElementById(
                "comboPopup"
            );


        // ================================
        // 문제 데이터 로딩
        // ================================

        document
            .querySelectorAll(
                ".question-data"
            )
            .forEach(element => {

                game.questions.push({

                    questionId:
                        Number(
                            element.dataset.questionId
                        ),

                    word:
                    element.dataset.word
                });
            });


        totalDisplay.textContent =
            game.questions.length;


        // ================================
        // 시작
        // ================================

        document
            .getElementById(
                "startButton"
            )
            .addEventListener(
                "click",
                async () => {

                    document
                        .getElementById(
                            "startOverlay"
                        )
                        .classList
                        .add(
                            "hidden"
                        );


                    field.focus();


                    game.gameRunning =
                        true;


                    await startRound();


                    startGameLoop();
                }
            );


        // ================================
        // 키보드
        // ================================

        document.addEventListener(
            "keydown",
            event => {

                if (!game.gameRunning) {
                    return;
                }


                if (
                    event.key === "ArrowLeft" ||
                    event.key.toLowerCase() === "a"
                ) {

                    event.preventDefault();

                    game.leftPressed =
                        true;
                }


                if (
                    event.key === "ArrowRight" ||
                    event.key.toLowerCase() === "d"
                ) {

                    event.preventDefault();

                    game.rightPressed =
                        true;
                }
            }
        );


        document.addEventListener(
            "keyup",
            event => {

                if (
                    event.key === "ArrowLeft" ||
                    event.key.toLowerCase() === "a"
                ) {

                    game.leftPressed =
                        false;
                }


                if (
                    event.key === "ArrowRight" ||
                    event.key.toLowerCase() === "d"
                ) {

                    game.rightPressed =
                        false;
                }
            }
        );


        // ================================
        // 모바일 버튼
        // ================================

        setupMoveButton(
            "leftButton",
            "leftPressed"
        );


        setupMoveButton(
            "rightButton",
            "rightPressed"
        );


        function setupMoveButton(
            id,
            stateName
        ) {

            const button =
                document.getElementById(
                    id
                );


            const start =
                event => {

                    event.preventDefault();

                    game[stateName] =
                        true;
                };


            const end =
                event => {

                    event.preventDefault();

                    game[stateName] =
                        false;
                };


            button.addEventListener(
                "pointerdown",
                start
            );


            button.addEventListener(
                "pointerup",
                end
            );


            button.addEventListener(
                "pointerleave",
                end
            );


            button.addEventListener(
                "pointercancel",
                end
            );
        }


        // ================================
        // 라운드 시작
        // ================================

        async function startRound() {

            game.roundRunning =
                false;


            clearObjects();


            const question =
                game.questions[
                    game.currentQuestionIndex
                    ];


            if (!question) {

                finishGame();

                return;
            }


            targetWord.textContent =
                question.word;


            currentDisplay.textContent =
                game.currentQuestionIndex + 1;


            updateProgress();


            showMessage(
                `${question.word}의 뜻을 찾아라!`
            );


            try {

                const response =
                    await fetch(
                        `/student/exams/word-game-take/round`
                        + `?attemptId=${attemptId}`
                        + `&questionId=${question.questionId}`
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


                game.roundRunning =
                    true;


                spawnOptions(
                    result.options
                );


            } catch (error) {

                console.error(
                    error
                );


                alert(
                    "게임 문제를 불러오는 중 오류가 발생했습니다."
                );


                game.gameRunning =
                    false;
            }
        }


        // ================================
        // 오브젝트 생성
        // ================================

        function spawnOptions(
            options
        ) {

            const fieldWidth =
                field.clientWidth;


            const objectWidth =
                fieldWidth < 700
                    ? 115
                    : 150;


            const spacing =
                fieldWidth /
                options.length;


            const shuffled =
                [...options];


            shuffled.forEach(
                (
                    answer,
                    index
                ) => {

                    const element =
                        document.createElement(
                            "div"
                        );


                    element.className =
                        "falling-word";


                    element.textContent =
                        answer;


                    const baseX =
                        spacing *
                        index
                        +
                        spacing / 2
                        -
                        objectWidth / 2;


                    const randomOffset =
                        (
                            Math.random() *
                            50
                        ) - 25;


                    let x =
                        baseX +
                        randomOffset;


                    x =
                        Math.max(
                            5,
                            Math.min(
                                fieldWidth -
                                objectWidth -
                                5,
                                x
                            )
                        );


                    const y =
                        -80;


                    element.style.left =
                        `${x}px`;


                    element.style.top =
                        "0px";


                    objectLayer
                        .appendChild(
                            element
                        );


                    game.objects.push({

                        element,

                        answer,

                        x,

                        y,

                        speed:
                            calculateFallSpeed(),

                        checked:
                            false
                    });
                }
            );
        }


        function calculateFallSpeed() {

            const progressRatio =
                game.currentQuestionIndex /
                Math.max(
                    game.questions.length,
                    1
                );


            return (
                0.12 +
                progressRatio * 0.08
            );
        }


        // ================================
        // 게임 루프
        // ================================

        function startGameLoop() {

            game.lastFrameTime =
                performance.now();


            game.frameId =
                requestAnimationFrame(
                    gameLoop
                );
        }


        function gameLoop(
            timestamp
        ) {

            if (!game.gameRunning) {
                return;
            }


            const delta =
                Math.min(
                    timestamp -
                    game.lastFrameTime,
                    35
                );


            game.lastFrameTime =
                timestamp;


            updatePlayer(
                delta
            );


            if (game.roundRunning) {

                updateObjects(
                    delta
                );


                detectCollisions();
            }


            game.frameId =
                requestAnimationFrame(
                    gameLoop
                );
        }


        // ================================
        // 캐릭터 이동
        // ================================

        function updatePlayer(
            delta
        ) {

            if (game.leftPressed) {

                game.playerX -=
                    game.playerSpeed *
                    delta *
                    0.055;
            }


            if (game.rightPressed) {

                game.playerX +=
                    game.playerSpeed *
                    delta *
                    0.055;
            }


            game.playerX =
                Math.max(
                    4,
                    Math.min(
                        96,
                        game.playerX
                    )
                );


            player.style.left =
                `${game.playerX}%`;
        }


        // ================================
        // 낙하
        // ================================

        function updateObjects(
            delta
        ) {

            const fieldHeight =
                field.clientHeight;

            let reachedBottom =
                false;


            game.objects.forEach(
                object => {

                    if (object.checked) {
                        return;
                    }


                    object.y +=
                        object.speed *
                        delta;


                    object.element
                        .style
                        .transform =
                        `translateY(${object.y}px)`;


                    if (
                        object.y >
                        fieldHeight
                    ) {

                        reachedBottom =
                            true;
                    }
                }
            );


            // 보기 중 하나라도 화면 아래까지 내려가면
            // 모든 보기를 동시에 위로 되돌린다.
            if (reachedBottom) {

                game.objects.forEach(
                    object => {

                        if (object.checked) {
                            return;
                        }

                        object.y = -80;

                        object.element
                            .style
                            .transform =
                            `translateY(${object.y}px)`;
                    }
                );
            }
        }


        // ================================
        // 충돌
        // ================================

        function detectCollisions() {

            if (!game.roundRunning) {
                return;
            }


            const playerRect =
                player
                    .getBoundingClientRect();


            game.objects.forEach(
                object => {

                    if (object.checked) {
                        return;
                    }


                    const rect =
                        object
                            .element
                            .getBoundingClientRect();


                    if (
                        rect.right >
                        playerRect.left + 8
                        &&
                        rect.left <
                        playerRect.right - 8
                        &&
                        rect.bottom >
                        playerRect.top + 12
                        &&
                        rect.top <
                        playerRect.bottom - 5
                    ) {

                        object.checked =
                            true;


                        handleCollision(
                            object
                        );
                    }
                }
            );
        }


        // ================================
        // 서버 정답 확인
        // ================================

        async function handleCollision(
            object
        ) {

            if (!game.roundRunning) {
                return;
            }


            game.roundRunning =
                false;


            const question =
                game.questions[
                    game.currentQuestionIndex
                    ];


            try {

                const csrfToken =
                    document.querySelector(
                        'meta[name="_csrf"]'
                    ).content;


                const csrfHeader =
                    document.querySelector(
                        'meta[name="_csrf_header"]'
                    ).content;


                const response =
                    await fetch(
                        "/student/exams/word-game-take/check",
                        {
                            method:
                                "POST",

                            headers: {

                                "Content-Type":
                                    "application/json",

                                [csrfHeader]:
                                csrfToken
                            },

                            body:
                                JSON.stringify({

                                    attemptId,

                                    questionId:
                                    question.questionId,

                                    studentAnswer:
                                    object.answer
                                })
                        }
                    );


                if (!response.ok) {

                    throw new Error(
                        "정답 확인 실패"
                    );
                }


                const result =
                    await response.json();


                if (!result.success) {

                    throw new Error(
                        result.message
                    );
                }


                game.answers.push({

                    questionId:
                    question.questionId,

                    studentAnswer:
                    object.answer
                });


                if (result.correct) {

                    handleCorrect();

                } else {

                    handleWrong();
                }


                setTimeout(
                    nextRound,
                    850
                );


            } catch (error) {

                console.error(
                    error
                );


                object.checked =
                    false;


                game.roundRunning =
                    true;


                showMessage(
                    "정답 확인에 실패했습니다."
                );
            }
        }


        // ================================
        // 정답
        // ================================

        function handleCorrect() {

            game.correctCount++;

            game.combo++;


            if (
                game.combo >
                game.maxCombo
            ) {

                game.maxCombo =
                    game.combo;
            }


            const comboBonus =
                Math.min(
                    game.combo * 10,
                    100
                );


            game.score +=
                100 +
                comboBonus;


            player.classList
                .remove(
                    "hit"
                );


            void player.offsetWidth;


            player.classList
                .add(
                    "caught"
                );


            showMessage(
                `✨ 정답! +${100 + comboBonus}`
            );


            showComboPopup();


            updateHud();
        }


        // ================================
        // 오답
        // ================================

        function handleWrong() {

            game.combo = 0;


            if (game.hearts > 0) {

                game.hearts--;
            }


            player.classList
                .remove(
                    "caught"
                );


            void player.offsetWidth;


            player.classList
                .add(
                    "hit"
                );


            showMessage(
                "💥 오답! 다음 문제에서 만회해보자!"
            );


            updateHud();
        }


        // ================================
        // 다음 문제
        // ================================

        async function nextRound() {

            game.currentQuestionIndex++;


            if (
                game.currentQuestionIndex >=
                game.questions.length
            ) {

                finishGame();

                return;
            }


            await startRound();
        }


        // ================================
        // 상태
        // ================================

        function updateHud() {

            scoreDisplay.textContent =
                game.score.toLocaleString();


            comboDisplay.textContent =
                game.combo;


            lifeDisplay.textContent =
                "❤️".repeat(
                    game.hearts
                )
                +
                "🤍".repeat(
                    3 -
                    game.hearts
                );
        }


        function updateProgress() {

            const percentage =
                (
                    game.currentQuestionIndex /
                    game.questions.length
                ) * 100;


            progress.style.width =
                `${percentage}%`;
        }


        function showMessage(
            message
        ) {

            gameMessage.textContent =
                message;


            gameMessage
                .classList
                .add(
                    "show"
                );


            clearTimeout(
                game.messageTimer
            );


            game.messageTimer =
                setTimeout(
                    () => {

                        gameMessage
                            .classList
                            .remove(
                                "show"
                            );

                    },
                    1300
                );
        }


        function showComboPopup() {

            if (game.combo < 2) {
                return;
            }


            comboPopup.textContent =
                `🔥 ${game.combo} COMBO!`;


            comboPopup
                .classList
                .remove(
                    "hidden"
                );


            void comboPopup.offsetWidth;


            comboPopup
                .classList
                .add(
                    "combo-popup"
                );


            setTimeout(
                () => {

                    comboPopup
                        .classList
                        .add(
                            "hidden"
                        );

                },
                650
            );
        }


        // ================================
        // 오브젝트 제거
        // ================================

        function clearObjects() {

            game.objects
                .forEach(
                    object => {

                        object.element
                            .remove();
                    }
                );


            game.objects = [];
        }


        // ================================
        // 게임 종료
        // ================================

        function finishGame() {

            game.gameRunning =
                false;


            game.roundRunning =
                false;


            if (game.frameId) {

                cancelAnimationFrame(
                    game.frameId
                );
            }


            clearObjects();


            progress.style.width =
                "100%";


            document
                .getElementById(
                    "finalGameScore"
                )
                .textContent =
                game.score
                    .toLocaleString();


            document
                .getElementById(
                    "finalCorrectCount"
                )
                .textContent =
                `${game.correctCount}/${game.questions.length}`;


            document
                .getElementById(
                    "finalMaxCombo"
                )
                .textContent =
                game.maxCombo;


            document
                .getElementById(
                    "completeOverlay"
                )
                .classList
                .remove(
                    "hidden"
                );
        }


        // ================================
        // 최종 시험 제출
        // ================================

        document
            .getElementById(
                "submitResultButton"
            )
            .addEventListener(
                "click",
                submitExam
            );


        async function submitExam() {

            const button =
                document.getElementById(
                    "submitResultButton"
                );


            button.disabled =
                true;


            button.textContent =
                "결과 저장 중...";


            try {

                const csrfToken =
                    document.querySelector(
                        'meta[name="_csrf"]'
                    ).content;


                const csrfHeader =
                    document.querySelector(
                        'meta[name="_csrf_header"]'
                    ).content;


                const response =
                    await fetch(
                        "/student/exams/submit",
                        {
                            method:
                                "POST",

                            headers: {

                                "Content-Type":
                                    "application/json",

                                [csrfHeader]:
                                csrfToken
                            },

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
                        "시험 결과 저장 실패"
                    );
                }


                const result =
                    await response.json();


                if (!result.success) {

                    throw new Error(
                        result.message ||
                        "시험 결과 저장 실패"
                    );
                }


                location.href =
                    "/student/exams";


            } catch (error) {

                console.error(
                    error
                );


                alert(
                    "시험 결과를 저장하는 중 오류가 발생했습니다."
                );


                button.disabled =
                    false;


                button.textContent =
                    "시험 결과 확인";
            }
        }

    }
);