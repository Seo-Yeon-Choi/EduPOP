document.addEventListener("DOMContentLoaded", function () {
    const classItems = Array.from(
        document.querySelectorAll(
            ".home-briefing-class-item"
        )
    );

    if (classItems.length === 0) {
        return;
    }

    const selectedClassName =
        document.getElementById(
            "homeSelectedClassName"
        );

    const warningReason =
        document.getElementById(
            "homeWarningReason"
        );

    const signalLink =
        document.getElementById(
            "homeClassSignalLink"
        );

    const parentReportButton =
        document.getElementById(
            "homeParentReportButton"
        );

    let currentClassId = null;
    let currentClassName = null;

    const signalConfig = {
        RED: {
            badgeText: "🔴 위험",
            buttonText: "🔴 우선 보완 필요 (위험)　👉 취약 학생 보기",
            className: "is-red"
        },
        YELLOW: {
            badgeText: "🟡 주의",
            buttonText: "🟡 확인 필요 (주의)　👉 점검 학생 보기",
            className: "is-yellow"
        },
        GREEN: {
            badgeText: "🟢 안정",
            buttonText: "🟢 안정 성취　👉 학생별 리포트 보기",
            className: "is-green"
        }
    };

    async function requestWarningSignal(classId) {
        const response = await fetch(
            `/analytics/api/class/${encodeURIComponent(classId)}/warning-signal`,
            {
                method: "GET",
                headers: {
                    "Accept": "application/json"
                },
                credentials: "same-origin"
            }
        );

        if (!response.ok) {
            throw new Error(
                `보완 신호 조회 실패: ${response.status}`
            );
        }

        return response.json();
    }

    function applyLeftBadge(item, level) {
        const badge =
            item.querySelector(
                ".home-class-signal"
            );

        const config =
            signalConfig[level] ||
            signalConfig.GREEN;

        badge.className =
            `home-class-signal ${config.className}`;

        badge.textContent =
            config.badgeText;
    }

    async function loadClassBadge(item) {
        const classId =
            item.dataset.classId;

        try {
            const data =
                await requestWarningSignal(classId);

            applyLeftBadge(
                item,
                data.warningLevel
            );

        } catch (error) {
            console.error(error);

            const badge =
                item.querySelector(
                    ".home-class-signal"
                );

            badge.className =
                "home-class-signal is-error";

            badge.textContent =
                "조회 실패";
        }
    }

    async function selectClass(item) {
        classItems.forEach(function (classItem) {
            classItem.classList.remove("active");
        });

        item.classList.add("active");

        currentClassId =
            item.dataset.classId;

        currentClassName =
            item.dataset.className;

        selectedClassName.textContent =
            currentClassName;

        signalLink.className =
            "home-class-signal-button is-loading";

        signalLink.textContent =
            "분석 중";

        warningReason.className =
            "home-briefing-warning is-loading";

        warningReason.textContent =
            "선택한 반의 종합 성취도를 분석하고 있습니다.";

        signalLink.href =
            `/analytics/class-trend?classId=${encodeURIComponent(currentClassId)}`;

        try {
            const data =
                await requestWarningSignal(
                    currentClassId
                );

            const level =
                data.warningLevel || "GREEN";

            const config =
                signalConfig[level] ||
                signalConfig.GREEN;

            signalLink.className =
                `home-class-signal-button ${config.className}`;

            signalLink.textContent =
                config.buttonText;

            warningReason.className =
                `home-briefing-warning ${config.className}`;

            warningReason.textContent =
                data.warningReason ||
                "표시할 보완 신호가 없습니다.";

            applyLeftBadge(
                item,
                level
            );

        } catch (error) {
            console.error(error);

            signalLink.className =
                "home-class-signal-button is-error";

            signalLink.textContent =
                "분석 결과 조회 실패";

            warningReason.className =
                "home-briefing-warning is-error";

            warningReason.textContent =
                "보완 신호를 불러오지 못했습니다.";
        }
    }

    classItems.forEach(function (item) {
        item.addEventListener(
            "click",
            function () {
                selectClass(item);
            }
        );

        loadClassBadge(item);
    });

    parentReportButton.addEventListener(
        "click",
        async function () {
            if (!currentClassId) {
                alert(
                    "반을 먼저 선택해주세요."
                );

                return;
            }

            const reportUrl =
                `${window.location.origin}/share/reports/auth?classId=${currentClassId}`;

            try {
                await navigator.clipboard.writeText(
                    reportUrl
                );

                alert(
                    `[${currentClassName}] 학부모 리포트 링크가 복사되었습니다.`
                );

            } catch (error) {
                console.error(error);

                alert(
                    "리포트 링크 복사에 실패했습니다."
                );
            }
        }
    );

    selectClass(classItems[0]);
});