"use strict";

document.addEventListener("DOMContentLoaded", function () {
    // DOMContentLoaded(디오엠 콘텐츠 로디드): HTML 구성이 끝난 뒤 경험치 카드 실행

    const card = document.getElementById("expCard");

    if (card === null) {
        return; // 현재 화면에 경험치 카드가 없으면 실행 종료
    }

    const loading = document.getElementById("expLoading");
    const errorBox = document.getElementById("expError");
    const errorMessage = document.getElementById("expErrorMessage");
    const retryButton = document.getElementById("expRetryButton");
    const content = document.getElementById("expContent");
    const characterBackground = document.getElementById("expCharacterBackground");
    const characterImage = document.getElementById("expCharacterImage");
    const characterPlaceholder = document.getElementById("expCharacterPlaceholder");
    const stageBadge = document.getElementById("expStageBadge");
    const stageName = document.getElementById("expStageName");
    const totalExp = document.getElementById("expTotal");
    const nextExp = document.getElementById("expNext");
    const progress = document.getElementById("expProgress");
    const progressBar = document.getElementById("expProgressBar");
    const progressText = document.getElementById("expProgressText");
    const guide = document.getElementById("expGuide");

    const requiredElements = [
        loading,
        errorBox,
        errorMessage,
        retryButton,
        content,
        characterBackground,
        characterImage,
        characterPlaceholder,
        stageBadge,
        stageName,
        totalExp,
        nextExp,
        progress,
        progressBar,
        progressText,
        guide
    ];

    if (requiredElements.some(function (element) {
        return element === null;
    })) {
        return; // 카드에 필요한 HTML 요소가 빠져 있으면 오류 발생 전에 실행 종료
    }

    retryButton.addEventListener("click", loadExp);
    // 다시 불러오기 버튼을 누르면 경험치 재조회

    loadExp(); // 학생 메인 화면이 열리면 경험치 정보 조회


    async function loadExp() {
        // async(어싱크): 서버 응답을 기다리는 동안 화면이 멈추지 않는 함수

        showLoading(); // 경험치 조회를 시작하기 전에 로딩 화면 표시

        try {
            const response = await fetch("/api/exp/me", {
                // fetch(페치): Controller의 경험치 조회 주소로 요청 전송
                method: "GET",
                credentials: "same-origin",
                headers: {
                    "Accept": "application/json"
                }
            });

            if (!response.ok) {
                throw new Error(
                    findErrorMessage(
                        response.status
                    )
                );
            }

            const responseType =
                response.headers.get(
                    "content-type"
                );

            if (responseType === null
                || !responseType.includes("application/json")) {

                throw new Error(
                    "경험치 응답 형식이 올바르지 않습니다."
                );
            }

            const expInfo =
                await response.json();
            // await(어웨이트): 서버가 보낸 JSON 경험치 정보가 도착할 때까지 기다림

            fillExpCard(
                expInfo
            ); // 조회한 경험치 정보를 카드에 표시

            showContent();
            // 로딩 화면을 숨기고 경험치 카드 본문 표시

        } catch (error) {
            const message =
                error instanceof Error
                    ? error.message
                    : "성장 정보를 불러오지 못했습니다.";

            showError(
                message
            ); // 조회 실패 이유를 오류 영역에 표시
        }
    }


    function fillExpCard(
        expInfo
    ) {
        const currentStage =
            readStage(
                expInfo.characterStage
            );

        const currentStageName =
            readStageName(
                expInfo.stageName,
                currentStage
            );

        const currentTotalExp =
            readPositiveNumber(
                expInfo.totalExp,
                "총 경험치"
            );

        const expToNextStage =
            readPositiveNumber(
                expInfo.expToNextStage,
                "다음 단계 경험치"
            );

        const progressPercent =
            readPercent(
                expInfo.expProgressPercent
            );

        const isMaxStage =
            expInfo.maxStage === true
            || currentStage === 6;

        card.dataset.stage =
            String(
                currentStage
            ); // 현재 단계에 맞는 CSS 색상 적용

        stageBadge.textContent =
            currentStage + "단계";
        // 현재 캐릭터 단계 표시

        changeStageName(
            currentStageName
        ); // G부터 GROW UP까지 각 글자를 개별 색상 요소로 표시

        totalExp.textContent =
            formatNumber(
                currentTotalExp
            ) + " EXP";
        // 현재 총 경험치 표시

        if (isMaxStage) {
            nextExp.textContent =
                "최종 단계를 달성했습니다.";

            progressText.textContent =
                "최종 단계 달성 100%";

            guide.textContent =
                "GROW UP을 완성했습니다. 앞으로도 학습 활동을 이어가 보세요.";

            changeProgress(
                100
            ); // 최종 단계이면 진행 막대를 100%로 표시

        } else {
            nextExp.textContent =
                "다음 단계까지 "
                + formatNumber(
                    expToNextStage
                )
                + " EXP";

            progressText.textContent =
                "현재 단계 진행률 "
                + progressPercent
                + "%";

            guide.textContent =
                "시험·복습·독서 활동으로 경험치를 모아 성장 공간을 완성해 보세요.";

            changeProgress(
                progressPercent
            ); // 현재 단계의 경험치 진행률 표시
        }

        changeCharacter(
            expInfo.characterImageUrl,
            currentStageName
        ); // 공통 캐릭터 이미지 표시

        changeBackground(
            expInfo.backgroundImageUrl,
            currentStage
        ); // 현재 단계에 맞는 배경 이미지 표시
    }

    function changeStageName(
        currentStageName
    ) {
        // changeStageName(체인지 스테이지 네임): 단계 이름을 글자별 색상 요소로 분리

        const letterClassNames = {
            G: "g",
            R: "r",
            O: "o",
            W: "w",
            U: "u",
            P: "p"
        };
        // letterClassNames(레터 클래스 네임즈): 성장 글자와 CSS 클래스 이름 연결

        stageName.textContent = "";
        // 기존 단계 이름을 지우고 현재 단계 글자로 다시 구성

        Array.from(
            currentStageName
        ).forEach(function (letter) {
            // Array.from(어레이 프롬): GROW UP 단계 이름을 한 글자씩 분리

            const className =
                letterClassNames[letter];
            // className(클래스 네임): 현재 글자에 사용할 고정 색상 클래스 이름

            if (className === undefined) {
                stageName.appendChild(
                    document.createTextNode(
                        letter
                    )
                );
                // 띄어쓰기처럼 색상 클래스가 필요 없는 문자는 그대로 추가

                return;
            }

            const letterElement =
                document.createElement(
                    "span"
                );
            // letterElement(레터 엘리먼트): 글자 하나의 색상과 네온을 담당할 span 요소

            letterElement.classList.add(
                "exp-stage-letter",
                "exp-stage-letter-" + className
            );
            // 글자 공통 클래스와 글자별 고정 색상 클래스 추가

            letterElement.textContent =
                letter;
            // span 요소에 현재 성장 글자 저장

            stageName.appendChild(
                letterElement
            );
            // 완성한 글자 요소를 단계 이름 영역에 순서대로 추가
        });
    }

    function changeProgress(
        percent
    ) {
        progress.setAttribute(
            "aria-valuenow",
            String(
                percent
            )
        ); // 화면 읽기 기능에 현재 진행률 전달

        progressBar.style.width =
            percent + "%";
        // 경험치 진행 막대 길이 변경
    }


    function changeCharacter(
        imageUrl,
        currentStageName
    ) {
        const safeUrl =
            readImageUrl(
                imageUrl,
                "/images/exp/character.png"
            );

        characterImage.hidden = true;
        characterPlaceholder.hidden = false;

        characterPlaceholder.textContent =
            currentStageName
            + " 캐릭터 준비 중";

        characterImage.onload = function () {
            characterImage.hidden = false;
            characterPlaceholder.hidden = true;
        };
        // 캐릭터 이미지를 정상적으로 불러오면 임시 문구를 숨김

        characterImage.onerror = function () {
            characterImage.hidden = true;
            characterPlaceholder.hidden = false;
            characterImage.removeAttribute("src");
        };
        // 이미지가 아직 없으면 깨진 이미지 대신 임시 문구 표시

        characterImage.alt =
            currentStageName
            + " 성장 단계 캐릭터";

        characterImage.src =
            safeUrl;
        // 캐릭터 이미지 주소 적용
    }


    function changeBackground(
        imageUrl,
        currentStage
    ) {
        const safeUrl =
            readImageUrl(
                imageUrl,
                "/images/exp/stage"
                + currentStage
                + "-background.png"
            );

        const backgroundImage =
            new Image();

        characterBackground.style.backgroundImage =
            "";

        backgroundImage.onload = function () {
            if (card.dataset.stage
                === String(currentStage)) {

                characterBackground.style.backgroundImage =
                    "url('"
                    + safeUrl
                    + "')";
            }
        };
        // 배경 이미지를 정상적으로 불러온 경우에만 화면에 적용

        backgroundImage.onerror = function () {
            characterBackground.style.backgroundImage =
                "";
        };
        // 배경이 아직 없으면 CSS에 설정한 단계별 기본 색상 유지

        backgroundImage.src =
            safeUrl;
        // 단계별 배경 이미지 불러오기
    }


    function readStage(
        value
    ) {
        const stage =
            Number(
                value
            );

        if (!Number.isInteger(stage)
            || stage < 1
            || stage > 6) {

            throw new Error(
                "캐릭터 성장 단계 정보가 올바르지 않습니다."
            );
        }

        return stage;
    }


    function readStageName(
        value,
        stage
    ) {
        const defaultNames = [
            "",
            "G",
            "GR",
            "GRO",
            "GROW",
            "GROW U",
            "GROW UP"
        ];

        if (typeof value !== "string"
            || value.trim() === "") {

            return defaultNames[stage];
            // 단계 이름이 비어 있으면 정해진 기본 이름 사용
        }

        return value.trim();
    }


    function readPositiveNumber(
        value,
        itemName
    ) {
        const number =
            Number(
                value
            );

        if (!Number.isFinite(number)
            || number < 0) {

            throw new Error(
                itemName
                + " 정보가 올바르지 않습니다."
            );
        }

        return Math.floor(
            number
        ); // 화면에는 0 이상의 정수 경험치만 사용
    }


    function readPercent(
        value
    ) {
        const percent =
            readPositiveNumber(
                value,
                "경험치 진행률"
            );

        return Math.min(
            percent,
            100
        ); // 진행률이 100%를 넘지 않도록 제한
    }


    function readImageUrl(
        value,
        defaultUrl
    ) {
        if (typeof value !== "string") {
            return defaultUrl;
        }

        const imageUrl =
            value.trim();

        const safeImagePath =
            /^\/images\/exp\/[a-zA-Z0-9_-]+\.png$/;

        if (!safeImagePath.test(imageUrl)) {
            return defaultUrl;
            // 정해진 경험치 이미지 폴더 밖의 주소는 사용하지 않음
        }

        return imageUrl;
    }


    function formatNumber(
        value
    ) {
        return value.toLocaleString(
            "ko-KR"
        ); // 경험치를 1,000처럼 읽기 쉽게 표시
    }


    function findErrorMessage(
        status
    ) {
        if (status === 401) {
            return "로그인 후 성장 정보를 확인할 수 있습니다.";
        }

        if (status === 403) {
            return "승인된 학생 계정만 성장 정보를 확인할 수 있습니다.";
        }

        return "성장 정보를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.";
    }


    function showLoading() {
        card.setAttribute(
            "aria-busy",
            "true"
        );

        loading.hidden = false;
        errorBox.hidden = true;
        content.hidden = true;
    }


    function showContent() {
        card.setAttribute(
            "aria-busy",
            "false"
        );

        loading.hidden = true;
        errorBox.hidden = true;
        content.hidden = false;
    }


    function showError(
        message
    ) {
        card.setAttribute(
            "aria-busy",
            "false"
        );

        loading.hidden = true;
        content.hidden = true;
        errorBox.hidden = false;

        errorMessage.textContent =
            message;
    }
});