(function () {
    "use strict";

    function normalizePath(path) {
        if (!path) {
            return "/";
        }

        const clean = path.replace(/\/+$/, "");
        return clean || "/";
    }

    function setupAdminNavigation() {
        const currentPath = normalizePath(window.location.pathname);
        const items = document.querySelectorAll(".admin-nav-item[data-paths]");

        items.forEach((item) => {
            const paths = (item.dataset.paths || "")
                .split(",")
                .map(normalizePath)
                .filter(Boolean);

            const active = paths.some((path) => {
                if (path === "/main/adminMain" || path === "/adminMain") {
                    return currentPath === path;
                }

                return currentPath === path || currentPath.startsWith(path + "/");
            });

            item.classList.toggle("active", active);
            if (active) {
                item.setAttribute("aria-current", "page");
            } else {
                item.removeAttribute("aria-current");
            }
        });
    }

    function memberRows() {
        return Array.from(document.querySelectorAll("#userTableBody .member-row"));
    }

    function visibleMemberRows() {
        return memberRows().filter((row) => !row.classList.contains("is-hidden"));
    }

    function updateMemberCounts() {
        const rows = memberRows();
        const countBy = (key, value) => rows.filter((row) => row.dataset[key] === value).length;
        const setCount = (id, value) => {
            const target = document.getElementById(id);
            if (target) {
                target.textContent = String(value);
            }
        };

        setCount("memberCountAll", rows.length);
        setCount("memberCountPending", countBy("status", "PENDING"));
        setCount("memberCountStudent", countBy("role", "STUDENT"));
        setCount("memberCountTeacher", countBy("role", "TEACHER"));
    }

    function updateVisibleMemberCount() {
        const visible = visibleMemberRows().length;
        const target = document.getElementById("visibleMemberCount");
        const noResult = document.getElementById("memberNoResults");

        if (target) {
            target.textContent = String(visible);
        }

        if (noResult) {
            noResult.hidden = visible !== 0;
        }
    }

    function clearMemberSelection() {
        const selectAll = document.getElementById("selectAll");
        if (selectAll) {
            selectAll.checked = false;
            selectAll.indeterminate = false;
        }

        document.querySelectorAll(".userCheckbox").forEach((checkbox) => {
            checkbox.checked = false;
            checkbox.closest("tr")?.classList.remove("is-selected");
        });

        updateBatchBar();
    }

    function setActiveQuickFilter(filter) {
        document.querySelectorAll(".admin-filter-chip[data-filter]").forEach((button) => {
            button.classList.toggle("active", button.dataset.filter === filter);
        });
    }

    function applyMemberFilters() {
        const searchInput = document.getElementById("memberSearchInput");
        const roleFilter = document.getElementById("roleFilter");
        const statusFilter = document.getElementById("statusFilter");

        if (!roleFilter || !statusFilter) {
            return;
        }

        const keyword = (searchInput?.value || "").trim().toLowerCase();
        const role = roleFilter.value;
        const status = statusFilter.value;

        memberRows().forEach((row) => {
            const matchesKeyword = !keyword || (row.dataset.search || "").toLowerCase().includes(keyword);
            const matchesRole = role === "ALL" || row.dataset.role === role;
            const matchesStatus = status === "ALL" || row.dataset.status === status;
            row.classList.toggle("is-hidden", !(matchesKeyword && matchesRole && matchesStatus));
        });

        let quick = "ALL";
        if (!keyword && role === "ALL" && status === "PENDING") {
            quick = "PENDING";
        } else if (!keyword && status === "ALL" && (role === "STUDENT" || role === "TEACHER")) {
            quick = role;
        }
        setActiveQuickFilter(quick);
        clearMemberSelection();
        updateVisibleMemberCount();
    }

    function setMemberQuickFilter(filter) {
        const searchInput = document.getElementById("memberSearchInput");
        const roleFilter = document.getElementById("roleFilter");
        const statusFilter = document.getElementById("statusFilter");

        if (!roleFilter || !statusFilter) {
            return;
        }

        if (searchInput) {
            searchInput.value = "";
        }
        roleFilter.value = filter === "STUDENT" || filter === "TEACHER" ? filter : "ALL";
        statusFilter.value = filter === "PENDING" ? "PENDING" : "ALL";
        setActiveQuickFilter(filter);
        applyMemberFilters();
    }

    function resetMemberFilters() {
        const searchInput = document.getElementById("memberSearchInput");
        const roleFilter = document.getElementById("roleFilter");
        const statusFilter = document.getElementById("statusFilter");

        if (searchInput) {
            searchInput.value = "";
        }
        if (roleFilter) {
            roleFilter.value = "ALL";
        }
        if (statusFilter) {
            statusFilter.value = "ALL";
        }

        setActiveQuickFilter("ALL");
        applyMemberFilters();
    }

    function toggleAllUsers(checked) {
        visibleMemberRows().forEach((row) => {
            const checkbox = row.querySelector(".userCheckbox");
            if (checkbox) {
                checkbox.checked = checked;
                row.classList.toggle("is-selected", checked);
            }
        });
        updateBatchBar();
    }

    function updateBatchBar() {
        const checked = Array.from(document.querySelectorAll(".userCheckbox:checked"));
        const bar = document.getElementById("memberBulkBar");
        const count = document.getElementById("selectedMemberCount");
        const selectAll = document.getElementById("selectAll");
        const visibleCheckboxes = visibleMemberRows()
            .map((row) => row.querySelector(".userCheckbox"))
            .filter(Boolean);
        const visibleChecked = visibleCheckboxes.filter((checkbox) => checkbox.checked);

        checked.forEach((checkbox) => checkbox.closest("tr")?.classList.add("is-selected"));
        document.querySelectorAll(".userCheckbox:not(:checked)").forEach((checkbox) => {
            checkbox.closest("tr")?.classList.remove("is-selected");
        });

        if (count) {
            count.textContent = String(checked.length);
        }
        if (bar) {
            bar.classList.toggle("is-visible", checked.length > 0);
        }
        if (selectAll) {
            selectAll.checked = visibleCheckboxes.length > 0 && visibleChecked.length === visibleCheckboxes.length;
            selectAll.indeterminate = visibleChecked.length > 0 && visibleChecked.length < visibleCheckboxes.length;
        }
    }

    function validateBatchUpdate() {
        const checked = document.querySelectorAll(".userCheckbox:checked");
        const statusSelect = document.getElementById("batchStatus");
        const label = statusSelect?.options[statusSelect.selectedIndex]?.text || "선택한 상태";

        if (checked.length === 0) {
            window.alert("상태를 변경할 회원을 한 명 이상 선택해주세요.");
            return false;
        }

        return window.confirm(`선택한 ${checked.length}명의 회원 상태를 '${label}'로 변경하시겠습니까?`);
    }

    function confirmSingleStatus(form) {
        const select = form.querySelector("select[name='status']");
        const name = form.dataset.userName || "선택한 회원";
        const label = select?.options[select.selectedIndex]?.text || "선택한 상태";
        return window.confirm(`${name} 회원의 상태를 '${label}'로 변경하시겠습니까?`);
    }

    function initMemberManagement() {
        const table = document.getElementById("userTableBody");
        if (!table) {
            return;
        }

        updateMemberCounts();

        document.querySelectorAll(".userCheckbox").forEach((checkbox) => {
            checkbox.addEventListener("change", updateBatchBar);
        });

        const params = new URLSearchParams(window.location.search);
        const requestedStatus = (params.get("status") || "").toUpperCase();
        if (["PENDING", "ACTIVE", "INACTIVE", "WITHDRAWN"].includes(requestedStatus)) {
            const statusFilter = document.getElementById("statusFilter");
            if (statusFilter) {
                statusFilter.value = requestedStatus;
            }
            setActiveQuickFilter(requestedStatus === "PENDING" ? "PENDING" : "ALL");
        }

        applyMemberFilters();
    }

    window.applyMemberFilters = applyMemberFilters;
    window.setMemberQuickFilter = setMemberQuickFilter;
    window.resetMemberFilters = resetMemberFilters;
    window.toggleAllUsers = toggleAllUsers;
    window.updateBatchBar = updateBatchBar;
    window.clearMemberSelection = clearMemberSelection;
    window.validateBatchUpdate = validateBatchUpdate;
    window.confirmSingleStatus = confirmSingleStatus;

    document.addEventListener("DOMContentLoaded", () => {
        setupAdminNavigation();
        initMemberManagement();
    });
})();
