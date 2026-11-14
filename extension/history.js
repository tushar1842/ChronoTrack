const API_URL = "http://127.0.0.1:5000/api/history";

let allHistory = [];

const historyList = document.getElementById("historyList");

const searchInput = document.getElementById("searchInput");
const modeFilter = document.getElementById("modeFilter");
const dateFrom = document.getElementById("dateFrom");
const dateTo = document.getElementById("dateTo");
const sortFilter = document.getElementById("sortFilter");

// ------------------------------
// LOAD USER
// ------------------------------

async function checkUser() {

    const data = await chrome.storage.local.get("chronotrackUser");

    if (!data.chronotrackUser) {
        window.location.href = "login.html";
        return false;
    }

    return true;
}

// ------------------------------
// FETCH HISTORY
// ------------------------------

async function fetchHistory() {

    try {

        historyList.innerHTML =
            '<div class="empty">Loading browsing history...</div>';

        const response = await fetch(API_URL);

        if (!response.ok) {
            throw new Error("Backend error: " + response.status);
        }

        const records = await response.json();

        if (!Array.isArray(records)) {
            throw new Error("Invalid history response");
        }

        allHistory = records;

        updateSummary();

        applyFilters();

    } catch (error) {

        console.error("History Error:", error);

        historyList.innerHTML =
            '<div class="empty">Unable to load history. Check your backend connection.</div>';

    }
}

// ------------------------------
// SUMMARY
// ------------------------------

function updateSummary() {

    const normal = allHistory.filter(
        item => item.browserMode === "normal"
    ).length;

    const incognito = allHistory.filter(
        item => item.browserMode === "incognito"
    ).length;

    document.getElementById("totalRecords").textContent =
        allHistory.length;

    document.getElementById("normalRecords").textContent =
        normal;

    document.getElementById("incognitoRecords").textContent =
        incognito;

}

// ------------------------------
// FILTER HISTORY
// ------------------------------

function applyFilters() {

    const query = searchInput.value.toLowerCase().trim();

    const mode = modeFilter.value;

    const from = dateFrom.value
        ? new Date(dateFrom.value + "T00:00:00").getTime()
        : null;

    const to = dateTo.value
        ? new Date(dateTo.value + "T23:59:59.999").getTime()
        : null;

    let filtered = allHistory.filter(item => {

        const title = (item.title || "").toLowerCase();
        const url = (item.url || "").toLowerCase();
        const domain = (item.domain || "").toLowerCase();

        const matchesSearch =
            title.includes(query) ||
            url.includes(query) ||
            domain.includes(query);

        const matchesMode =
            mode === "all" || item.browserMode === mode;

        const timestamp = new Date(item.visitTime).getTime();

        const matchesFrom =
            from === null ||
            (Number.isFinite(timestamp) && timestamp >= from);

        const matchesTo =
            to === null ||
            (Number.isFinite(timestamp) && timestamp <= to);

        return matchesSearch &&
            matchesMode &&
            matchesFrom &&
            matchesTo;

    });

    // SORT

    filtered.sort((a, b) => {

        const first = new Date(a.visitTime).getTime();
        const second = new Date(b.visitTime).getTime();

        return sortFilter.value === "oldest"
            ? first - second
            : second - first;

    });

    displayHistory(filtered);

}

// ------------------------------
// DISPLAY HISTORY
// ------------------------------

function displayHistory(records) {

    historyList.innerHTML = "";

    document.getElementById("resultsInfo").textContent =
        "Showing " + records.length + " of " + allHistory.length + " records";

    if (records.length === 0) {

        historyList.innerHTML =
            '<div class="empty">No matching browsing records found.</div>';

        return;

    }

    records.forEach(item => {

        const row = document.createElement("div");

        row.className = "history-item";

        const left = document.createElement("div");

        left.className = "history-row";

        const icon = document.createElement("div");

        icon.className = "icon";

        icon.textContent = "🌐";

        const info = document.createElement("div");

        info.className = "info";

        const title = document.createElement("h4");

        title.textContent =
            item.title || item.domain || "Unknown Website";

        const url = document.createElement("p");

        url.textContent = item.url || "";

        const badge = document.createElement("span");

        badge.className = "badge";

        if (item.browserMode === "incognito") {

            badge.textContent = "Incognito";
            badge.classList.add("incognito");

        } else {

            badge.textContent = "Normal";
            badge.classList.add("normal");

        }

        info.append(title, url, badge);

        left.append(icon, info);

        const time = document.createElement("span");

        time.className = "time";

        const visitDate = new Date(item.visitTime);

        time.textContent = Number.isNaN(visitDate.getTime())
            ? "Unknown time"
            : visitDate.toLocaleString([], {
                day: "2-digit",
                month: "short",
                year: "numeric",
                hour: "2-digit",
                minute: "2-digit"
            });

        row.append(left, time);

        historyList.appendChild(row);

    });

}

// ------------------------------
// EVENT LISTENERS
// ------------------------------

searchInput.addEventListener("input", applyFilters);

modeFilter.addEventListener("change", applyFilters);

dateFrom.addEventListener("change", applyFilters);

dateTo.addEventListener("change", applyFilters);

sortFilter.addEventListener("change", applyFilters);

// ------------------------------
// BACK TO DASHBOARD
// ------------------------------

document.getElementById("backBtn").addEventListener("click", () => {

    window.location.href = "dashboard.html";

});

// ------------------------------
// INITIALIZE
// ------------------------------

async function initialize() {

    const loggedIn = await checkUser();

    if (loggedIn) {
        await fetchHistory();
    }

}

initialize();