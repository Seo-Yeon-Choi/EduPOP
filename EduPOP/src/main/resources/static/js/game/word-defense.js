(() => {

    // =============================================
    // DOM
    // =============================================

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


    const monsterLayer =
        document.getElementById(
            "monsterLayer"
        );


    const battleField =
        document.getElementById(
            "battleField"
        );


    const castle =
        document.getElementById(
            "castle"
        );


    const attackEffect =
        document.getElementById(
            "attackEffect"
        );


    const battleMessage =
        document.getElementById(
            "battleMessage"
        );


    const hpDisplay =
        document.getElementById(
            "hpDisplay"
        );

    const scoreDisplay =
        document.getElementById(
            "scoreDisplay"
        );

    const comboDisplay =
        document.getElementById(
            "comboDisplay"
        );

    const waveDisplay =
        document.getElementById(
            "waveDisplay"
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

    const finalCorrect =
        document.getElementById(
            "finalCorrect"
        );


    // =============================================
    // STATE
    // =============================================

    const game = {

        questions: [],

        currentIndex: 0,

        score: 0,

        combo: 0,

        maxCombo: 0,

        hp: 3,

        correctCount: 0,

        answers: [],

        monsters: [],

        running: false,

        roundRunning: false,

        checking: false,

        frameId: null,

        lastTime: null

    };


    // =============================================
    // 문제 읽기
    // =============================================

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


    // =============================================
    // START
    // =============================================

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


        game.running =
            true;


        loadQuestion();
    }


    // =============================================
    // 문제 로드
    // =============================================

    async function loadQuestion() {

        if (
            game.currentIndex >=
            game.questions.length
        ) {

            completeGame();

            return;
        }


        game.roundRunning =
            false;

        game.checking =
            false;


        clearMonsters();


        const question =
            game.questions[
                game.currentIndex
                ];


        questionText.textContent =
            question.text;


        waveDisplay.textContent =
            `${game.currentIndex + 1} / ${game.questions.length}`;


        battleMessage.textContent =
            "정답 몬스터를 처치하세요!";


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


            spawnMonsters(
                result.options
            );


            game.roundRunning =
                true;

            game.lastTime =
                null;


            game.frameId =
                requestAnimationFrame(
                    gameLoop
                );


        } catch (error) {

            alert(
                error.message
            );
        }
    }


    // =============================================
    // 몬스터 생성
    // =============================================

    function spawnMonsters(
        options
    ) {

        monsterLayer.innerHTML =
            "";

        game.monsters =
            [];


        const fieldWidth =
            battleField.clientWidth;


        const count =
            options.length;


        const usableWidth =
            fieldWidth - 120;


        const spacing =
            usableWidth /
            Math.max(
                count,
                1
            );


        options.forEach(
            (
                answer,
                index
            ) => {

                const monster =
                    document.createElement(
                        "button"
                    );


                monster.type =
                    "button";

                monster.className =
                    "monster";


                monster.innerHTML = `

                    <span class="monster-icon">
                        ${getMonsterEmoji(index)}
                    </span>

                    <strong>
                        ${escapeHtml(answer)}
                    </strong>

                `;


                const x =
                    60 +
                    spacing *
                    index +
                    spacing / 2;


                monster.style.left =
                    `${x}px`;


                monsterLayer.appendChild(
                    monster
                );


                const monsterData = {

                    element:
                    monster,

                    answer,

                    x,

                    y: 45,

                    speed:
                        calculateMonsterSpeed(),

                    alive:
                        true

                };


                monster.addEventListener(
                    "click",
                    () => {

                        attackMonster(
                            monsterData
                        );
                    }
                );


                game.monsters.push(
                    monsterData
                );

            }
        );


        renderMonsters();
    }


    // =============================================
    // 몬스터 종류
    // =============================================

    function getMonsterEmoji(
        index
    ) {

        const monsters = [
            "👾",
            "👻",
            "👹",
            "🧟"
        ];


        return monsters[
        index %
        monsters.length
            ];
    }


    // =============================================
    // 속도
    // =============================================

    function calculateMonsterSpeed() {

        const progress =
            game.currentIndex /
            Math.max(
                game.questions.length,
                1
            );


        return (
            0.018 +
            progress *
            0.008
        );
    }


    // =============================================
    // LOOP
    // =============================================

    function gameLoop(
        time
    ) {

        if (
            !game.running ||
            !game.roundRunning
        ) {

            return;
        }


        if (
            game.lastTime === null
        ) {

            game.lastTime =
                time;
        }


        const delta =
            Math.min(
                time -
                game.lastTime,
                40
            );


        game.lastTime =
            time;


        updateMonsters(
            delta
        );


        renderMonsters();


        game.frameId =
            requestAnimationFrame(
                gameLoop
            );
    }


    // =============================================
    // 이동
    // =============================================

    function updateMonsters(
        delta
    ) {

        const fieldHeight =
            battleField.clientHeight;


        const castleLine =
            fieldHeight - 145;


        let reachedCastle =
            false;


        game.monsters.forEach(
            monster => {

                if (!monster.alive) {
                    return;
                }


                monster.y +=
                    monster.speed *
                    delta;


                if (
                    monster.y >=
                    castleLine
                ) {

                    reachedCastle =
                        true;
                }

            }
        );


        if (reachedCastle) {

            handleCastleHit();

        }
    }


    // =============================================
    // Rendering
    // =============================================

    function renderMonsters() {

        game.monsters.forEach(
            monster => {

                if (!monster.alive) {
                    return;
                }


                monster.element.style
                    .transform =
                    `translate(-50%, ${monster.y}px)`;

            }
        );
    }


    // =============================================
    // 성 도착
    // =============================================

    function handleCastleHit() {

        if (
            !game.roundRunning
        ) {
            return;
        }


        cancelAnimationFrame(
            game.frameId
        );


        game.hp =
            Math.max(
                0,
                game.hp - 1
            );


        game.combo =
            0;


        battleMessage.textContent =
            "💥 성이 공격받았습니다! 다시 막아보세요!";


        castle.classList.add(
            "damaged"
        );


        game.monsters.forEach(
            monster => {

                if (
                    monster.alive
                ) {

                    monster.y =
                        45;
                }

            }
        );


        renderMonsters();


        updateHud();


        setTimeout(
            () => {

                castle.classList.remove(
                    "damaged"
                );


                if (
                    game.hp <= 0
                ) {

                    game.hp =
                        3;

                    updateHud();
                }


                game.lastTime =
                    null;


                game.frameId =
                    requestAnimationFrame(
                        gameLoop
                    );

            },
            500
        );
    }

    // =============================================
    // 몬스터 공격
    // =============================================

    async function attackMonster(
        monster
    ) {

        if (
            !game.roundRunning ||
            game.checking ||
            !monster.alive
        ) {

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
                                monster.answer

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

                handleCorrectAttack(
                    monster
                );

            } else {

                handleWrongAttack(
                    monster
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


    // =============================================
    // 정답 공격
    // =============================================

    function handleCorrectAttack(
        monster
    ) {

        game.roundRunning =
            false;


        cancelAnimationFrame(
            game.frameId
        );


        monster.alive =
            false;


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
            monster.answer

        });


        monster.element.classList.add(
            "defeated"
        );


        showAttackEffect(
            monster.element
        );


        battleMessage.textContent =
            `⚡ 정답! +${100 + comboBonus}`;


        updateHud();


        setTimeout(
            () => {

                game.currentIndex++;

                loadQuestion();

            },
            850
        );
    }


    // =============================================
    // 오답 공격
    // =============================================

    function handleWrongAttack(
        monster
    ) {

        game.combo =
            0;


        game.hp =
            Math.max(
                0,
                game.hp - 1
            );


        monster.element.classList.add(
            "wrong-hit"
        );


        castle.classList.add(
            "damaged"
        );


        battleMessage.textContent =
            "❌ 잘못된 몬스터를 공격했습니다!";


        updateHud();


        setTimeout(
            () => {

                monster.element.classList.remove(
                    "wrong-hit"
                );

                castle.classList.remove(
                    "damaged"
                );


                game.checking =
                    false;


                if (
                    game.hp <= 0
                ) {

                    game.hp =
                        3;

                    updateHud();
                }

            },
            500
        );
    }


    // =============================================
    // 공격 효과
    // =============================================

    function showAttackEffect(
        target
    ) {

        const fieldRect =
            battleField
                .getBoundingClientRect();

        const targetRect =
            target
                .getBoundingClientRect();


        attackEffect.style.left =
            `${
                targetRect.left -
                fieldRect.left +
                targetRect.width / 2
            }px`;


        attackEffect.style.top =
            `${
                targetRect.top -
                fieldRect.top
            }px`;


        attackEffect.classList.remove(
            "hidden"
        );


        attackEffect.classList.remove(
            "animate"
        );


        void attackEffect.offsetWidth;


        attackEffect.classList.add(
            "animate"
        );


        setTimeout(
            () => {

                attackEffect.classList.add(
                    "hidden"
                );

            },
            450
        );
    }


    // =============================================
    // HUD
    // =============================================

    function updateHud() {

        hpDisplay.textContent =
            "❤️".repeat(
                Math.max(
                    game.hp,
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


    // =============================================
    // 몬스터 제거
    // =============================================

    function clearMonsters() {

        cancelAnimationFrame(
            game.frameId
        );


        monsterLayer.innerHTML =
            "";


        game.monsters =
            [];
    }


    // =============================================
    // 완료
    // =============================================

    function completeGame() {

        game.running =
            false;

        game.roundRunning =
            false;


        cancelAnimationFrame(
            game.frameId
        );


        progressBar.style.width =
            "100%";

        progressText.textContent =
            "100%";


        finalScore.textContent =
            game.score;


        finalCombo.textContent =
            game.maxCombo;


        finalCorrect.textContent =
            `${game.correctCount} / ${game.questions.length}`;


        completeOverlay.classList.remove(
            "hidden"
        );
    }


    // =============================================
    // 제출
    // =============================================

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


    // =============================================
    // escape
    // =============================================

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


    // =============================================
    // EVENT
    // =============================================

    startButton.addEventListener(
        "click",
        startGame
    );


    submitButton.addEventListener(
        "click",
        submitExam
    );

})();