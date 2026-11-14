
const API_URL = "http://127.0.0.1:5000/api/history";

const historyList = document.getElementById("historyList");
const popularSites = document.getElementById("popularSites");

const searchInput = document.getElementById("searchHistory");
const modeFilter = document.getElementById("modeFilter");
const refreshBtn = document.getElementById("refreshBtn");

let allHistory = [];

// ------------------------------
// SIDEBAR NAVIGATION
// ------------------------------

const pageRoutes = {
    dashboard: "dashboard.html",
    history: "history.html",
    analytics: "analytics.html",
    settings: "settings.html"
};

document.querySelectorAll(".menu-item[data-page]").forEach(item => {

    item.addEventListener("click", () => {

        const selectedPage = item.dataset.page;
        const target = pageRoutes[selectedPage];

        if (!target) return;

        window.location.href = target;

    });

});

// ------------------------------
// USER PROFILE
// ------------------------------

async function loadUserProfile() {

    const data = await chrome.storage.local.get("chronotrackUser");

    const user = data.chronotrackUser;

    if (!user) {
        window.location.href = "login.html";
        return false;
    }

    document.getElementById("userName").textContent =
        user.name || "User";

    document.getElementById("userEmail").textContent =
        user.email || "";

    document.getElementById("welcomeName").textContent =
        user.name ? user.name.split(" ")[0] : "User";

    const image = document.getElementById("userImage");

    if (user.picture) {
        image.src = user.picture;
        image.style.display = "block";
    } else {
        image.style.display = "none";
    }

    return true;
}

// ------------------------------
// LOAD MONGODB HISTORY
// ------------------------------

async function loadHistory() {

    try {

        historyList.innerHTML =
            '<div class="empty">Loading browsing activity...</div>';

        const response = await fetch(API_URL);

        if (!response.ok) {
            throw new Error("Backend error: " + response.status);
        }

        const records = await response.json();

        if (!Array.isArray(records)) {
            throw new Error("Invalid API response");
        }

        allHistory = records;

        document.getElementById("extensionStatus").textContent =
            "Online";

        document.getElementById("connectionText").textContent =
            "Backend connected successfully";

        document.getElementById("lastUpdated").textContent =
            "Last updated: " + new Date().toLocaleTimeString();

        updateDashboard();

    } catch (error) {

        console.error("ChronoTrack Error:", error);

        document.getElementById("extensionStatus").textContent =
            "Offline";

        document.getElementById("connectionText").textContent =
            "Backend connection failed";

        historyList.innerHTML =
            '<div class="empty">Unable to load history. Make sure your backend is running.</div>';

        popularSites.innerHTML =
            '<div class="empty">Website data unavailable.</div>';

        document.getElementById("totalSites").textContent = "0";
        document.getElementById("todayVisits").textContent = "0";
        document.getElementById("normalVisits").textContent = "0";
        document.getElementById("incognitoVisits").textContent = "0";

    }

}

// ------------------------------
// DASHBOARD STATISTICS
// ------------------------------

function updateDashboard() {

    const now = Date.now();

    const last24Hours = now - 24 * 60 * 60 * 1000;

    const recentRecords = allHistory.filter(item => {

        const visitTime = new Date(item.visitTime).getTime();

        return Number.isFinite(visitTime) &&
            visitTime >= last24Hours &&
            visitTime <= now;

    });

    const uniqueSites = new Set();

    recentRecords.forEach(item => {

        if (item.domain) {
            uniqueSites.add(item.domain);
        }

    });

    const normalCount = recentRecords.filter(
        item => item.browserMode === "normal"
    ).length;

    const incognitoCount = recentRecords.filter(
        item => item.browserMode === "incognito"
    ).length;

    document.getElementById("totalSites").textContent =
        uniqueSites.size;

    document.getElementById("todayVisits").textContent =
        recentRecords.length;

    document.getElementById("normalVisits").textContent =
        normalCount;

    document.getElementById("incognitoVisits").textContent =
        incognitoCount;

    displayHistory();

    displayPopularSites(recentRecords);

}

// ------------------------------
// DISPLAY BROWSING HISTORY
// ------------------------------

function displayHistory() {

    const query = searchInput.value.toLowerCase().trim();

    const selectedMode = modeFilter.value;

    const filteredRecords = allHistory.filter(item => {

        const title = (item.title || "").toLowerCase();
        const url = (item.url || "").toLowerCase();
        const domain = (item.domain || "").toLowerCase();

        const matchesSearch =
            title.includes(query) ||
            url.includes(query) ||
            domain.includes(query);

        const matchesMode =
            selectedMode === "all" ||
            item.browserMode === selectedMode;

        return matchesSearch && matchesMode;

    });

    filteredRecords.sort((a, b) => {

        return new Date(b.visitTime).getTime() -
            new Date(a.visitTime).getTime();

    });

    historyList.innerHTML = "";

    if (filteredRecords.length === 0) {

        historyList.innerHTML =
            '<div class="empty">No matching browsing activity found.</div>';

        return;

    }

    filteredRecords.slice(0, 30).forEach(item => {

        const row = document.createElement("div");
        row.className = "history-item";

        const left = document.createElement("div");
        left.className = "history-row";

        const icon = document.createElement("div");
        icon.className = "site-icon";
        icon.textContent = "🌐";

        const info = document.createElement("div");
        info.className = "site-info";

        const title = document.createElement("h4");

        title.textContent =
            item.title || item.domain || "Unknown Website";

        const domain = document.createElement("p");

        domain.textContent = item.url || "";

        const mode = document.createElement("span");

        mode.className = "mode-badge";

        if (item.browserMode === "incognito") {

            mode.textContent = "Incognito";
            mode.classList.add("incognito-badge");

        } else {

            mode.textContent = "Normal";
            mode.classList.add("normal-badge");

        }

        info.append(title, domain, mode);

        left.append(icon, info);

        const time = document.createElement("span");

        time.className = "visit-time";

        const date = new Date(item.visitTime);

        time.textContent = Number.isNaN(date.getTime())
            ? "Unknown time"
            : date.toLocaleString([], {
                day: "2-digit",
                month: "short",
                hour: "2-digit",
                minute: "2-digit"
            });

        row.append(left, time);

        historyList.appendChild(row);

    });

}

// ------------------------------
// MOST VISITED WEBSITES
// ------------------------------

function displayPopularSites(records) {

    const counts = {};

    records.forEach(item => {

        const domain = item.domain;

        if (domain) {
            counts[domain] = (counts[domain] || 0) + 1;
        }

    });

    const sorted = Object.entries(counts)
        .sort((a, b) => b[1] - a[1])
        .slice(0, 7);

    popularSites.innerHTML = "";

    if (sorted.length === 0) {

        popularSites.innerHTML =
            '<div class="empty">No website data available.</div>';

        return;

    }

    sorted.forEach(([domain, count]) => {

        const row = document.createElement("div");
        row.className = "popular-item";

        const name = document.createElement("span");
        name.textContent = domain;

        const number = document.createElement("span");
        number.textContent = count + " visits";

        row.append(name, number);

        popularSites.appendChild(row);

    });

}

// ------------------------------
// SEARCH
// ------------------------------

searchInput.addEventListener("input", displayHistory);

// ------------------------------
// MODE FILTER
// ------------------------------

modeFilter.addEventListener("change", displayHistory);

// ------------------------------
// REFRESH
// ------------------------------

refreshBtn.addEventListener("click", async () => {

    refreshBtn.disabled = true;
    refreshBtn.textContent = "Loading...";

    await loadHistory();

    refreshBtn.disabled = false;
    refreshBtn.textContent = "↻ Refresh";

});

// ------------------------------
// LOGOUT
// ------------------------------

document.getElementById("logout").addEventListener("click", async () => {

    await chrome.storage.local.remove([
        "chronotrackUser",
        "googleAccessToken"
    ]);

    window.location.href = "login.html";

});

// ------------------------------
// INITIALIZE
// ------------------------------

async function initializeDashboard() {

    const loggedIn = await loadUserProfile();

    if (loggedIn) {
        await loadHistory();
    }

}

initializeDashboard();
