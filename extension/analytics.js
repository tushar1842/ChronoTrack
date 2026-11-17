const API_URL = "http://127.0.0.1:5000/api/history";

let chartData = {
    labels: [],
    values: [],
    normal: 0,
    incognito: 0
};

const $ = id => document.getElementById(id);

// ------------------------------
// USER AUTH CHECK
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
// DATE HELPERS
// ------------------------------

function dateKey(date) {

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");

    return `${year}-${month}-${day}`;
}

// ------------------------------
// FETCH DATA
// ------------------------------

async function loadAnalytics() {

    $("status").textContent = "Loading analytics...";

    try {

        const response = await fetch(API_URL);

        if (!response.ok) {
            throw new Error("Backend error: " + response.status);
        }

        const records = await response.json();

        if (!Array.isArray(records)) {
            throw new Error("Invalid API response");
        }

        processAnalytics(records);

        $("status").textContent =
            "Data updated: " + new Date().toLocaleString();

    } catch (error) {

        console.error("Analytics error:", error);

        $("status").textContent =
            "Unable to connect to backend. Check server.js.";

        $("topSites").innerHTML =
            '<div class="empty">Analytics data unavailable.</div>';

    }

}

// ------------------------------
// PROCESS ANALYTICS
// ------------------------------

function processAnalytics(records) {

    const now = new Date();

    const todayStart = new Date(
        now.getFullYear(),
        now.getMonth(),
        now.getDate()
    );

    const sevenDaysAgo = new Date(todayStart);

    sevenDaysAgo.setDate(sevenDaysAgo.getDate() - 6);

    const recent = records.filter(item => {

        const time = new Date(item.visitTime).getTime();

        return Number.isFinite(time) &&
            time >= sevenDaysAgo.getTime() &&
            time <= now.getTime();

    });

    const domains = new Set();

    const counts = {};

    let normal = 0;
    let incognito = 0;

    recent.forEach(item => {

        if (item.domain) {

            domains.add(item.domain);

            counts[item.domain] =
                (counts[item.domain] || 0) + 1;

        }

        if (item.browserMode === "normal") {
            normal++;
        }

        if (item.browserMode === "incognito") {
            incognito++;
        }

    });

    $("totalVisits").textContent = recent.length;

    $("uniqueSites").textContent = domains.size;

    $("normalVisits").textContent = normal;

    $("incognitoVisits").textContent = incognito;

    chartData.normal = normal;

    chartData.incognito = incognito;

    // DAILY COUNTS

    const dailyCounts = {};

    const labels = [];

    const values = [];

    for (let i = 0; i < 7; i++) {

        const date = new Date(sevenDaysAgo);

        date.setDate(date.getDate() + i);

        const key = dateKey(date);

        dailyCounts[key] = 0;

        labels.push(
            date.toLocaleDateString("en-IN", {
                day: "2-digit",
                month: "short"
            })
        );

    }

    recent.forEach(item => {

        const date = new Date(item.visitTime);

        const key = dateKey(date);

        if (dailyCounts[key] !== undefined) {
            dailyCounts[key]++;
        }

    });

    labels.forEach((label, index) => {

        const date = new Date(sevenDaysAgo);

        date.setDate(date.getDate() + index);

        values.push(dailyCounts[dateKey(date)]);

    });

    chartData.labels = labels;

    chartData.values = values;

    // TOP WEBSITES

    const topSites = Object.entries(counts)
        .sort((a, b) => b[1] - a[1])
        .slice(0, 10);

    displayTopSites(topSites);

    drawActivityChart();

    drawModeChart();

}

// ------------------------------
// TOP WEBSITES
// ------------------------------

function displayTopSites(sites) {

    const container = $("topSites");

    container.replaceChildren();

    if (sites.length === 0) {

        container.innerHTML =
            '<div class="empty">No website visits recorded in the last 7 days.</div>';

        return;

    }

    sites.forEach(([domain, count], index) => {

        const row = document.createElement("div");

        row.className = "site-item";

        const name = document.createElement("span");

        name.className = "site-name";

        name.textContent = `${index + 1}. ${domain}`;

        const visits = document.createElement("span");

        visits.className = "site-count";

        visits.textContent = `${count} visits`;

        row.append(name, visits);

        container.appendChild(row);

    });

}

// ------------------------------
// CANVAS SETUP
// ------------------------------

function setupCanvas(canvas) {

    const rect = canvas.getBoundingClientRect();

    const width = rect.width;

    const height = rect.height;

    const dpr = window.devicePixelRatio || 1;

    canvas.width = Math.round(width * dpr);

    canvas.height = Math.round(height * dpr);

    const ctx = canvas.getContext("2d");

    ctx.scale(dpr, dpr);

    return { ctx, width, height };

}

// ------------------------------
// ACTIVITY BAR CHART
// ------------------------------

function drawActivityChart() {

    const canvas = $("activityChart");

    const { ctx, width, height } = setupCanvas(canvas);

    ctx.clearRect(0, 0, width, height);

    const left = 35;
    const right = 12;
    const top = 20;
    const bottom = 38;

    const chartWidth = width - left - right;

    const chartHeight = height - top - bottom;

    const maxValue = Math.max(...chartData.values, 1);

    const maxScale = Math.ceil(maxValue / 5) * 5 || 5;

    // GRID LINES

    ctx.font = "10px Inter";

    ctx.textAlign = "right";

    for (let i = 0; i <= 4; i++) {

        const y = top + chartHeight * i / 4;

        const value = Math.round(maxScale * (4 - i) / 4);

        ctx.strokeStyle = "#26344c";

        ctx.beginPath();

        ctx.moveTo(left, y);

        ctx.lineTo(width - right, y);

        ctx.stroke();

        ctx.fillStyle = "#94a3b8";

        ctx.fillText(value, left - 8, y + 3);

    }

    // BARS

    const slotWidth = chartWidth / 7;

    const barWidth = Math.min(28, slotWidth * 0.55);

    chartData.values.forEach((value, index) => {

        const barHeight = value / maxScale * chartHeight;

        const x = left + slotWidth * index +
            (slotWidth - barWidth) / 2;

        const y = top + chartHeight - barHeight;

        ctx.fillStyle = "#38bdf8";

        ctx.beginPath();

        ctx.roundRect(
            x,
            y,
            barWidth,
            Math.max(barHeight, 0),
            5
        );

        ctx.fill();

        ctx.fillStyle = "#94a3b8";

        ctx.textAlign = "center";

        ctx.fillText(
            chartData.labels[index],
            x + barWidth / 2,
            height - 12
        );

    });

}

// ------------------------------
// DONUT CHART
// ------------------------------

function drawModeChart() {

    const canvas = $("modeChart");

    const { ctx, width, height } = setupCanvas(canvas);

    ctx.clearRect(0, 0, width, height);

    const normal = chartData.normal;

    const incognito = chartData.incognito;

    const total = normal + incognito;

    const cx = width / 2;

    const cy = height / 2;

    const radius = 82;

    const thickness = 20;

    ctx.lineWidth = thickness;

    ctx.lineCap = "butt";

    let startAngle = -Math.PI / 2;

    if (total === 0) {

        ctx.beginPath();

        ctx.strokeStyle = "#26344c";

        ctx.arc(cx, cy, radius, 0, Math.PI * 2);

        ctx.stroke();

    } else {

        const normalAngle = normal / total * Math.PI * 2;

        ctx.beginPath();

        ctx.strokeStyle = "#22d3ee";

        ctx.arc(
            cx,
            cy,
            radius,
            startAngle,
            startAngle + normalAngle
        );

        ctx.stroke();

        startAngle += normalAngle;

        ctx.beginPath();

        ctx.strokeStyle = "#c084fc";

        ctx.arc(
            cx,
            cy,
            radius,
            startAngle,
            startAngle + incognito / total * Math.PI * 2
        );

        ctx.stroke();

    }

    ctx.textAlign = "center";

    ctx.fillStyle = "#ffffff";

    ctx.font = "bold 25px Inter";

    ctx.fillText(total, cx, cy + 8);

    ctx.font = "10px Inter";

    ctx.fillStyle = "#94a3b8";

    ctx.fillText("VISITS", cx, cy + 25);

    const normalPercent = total
        ? Math.round(normal / total * 100)
        : 0;

    const incognitoPercent = total
        ? Math.round(incognito / total * 100)
        : 0;

    $("normalPercent").textContent = normalPercent + "%";

    $("incognitoPercent").textContent = incognitoPercent + "%";

}

// ------------------------------
// NAVIGATION
// ------------------------------

$("dashboardBtn").addEventListener("click", () => {

    window.location.href = "dashboard.html";

});

$("historyBtn").addEventListener("click", () => {

    window.location.href = "history.html";

});

// ------------------------------
// REFRESH
// ------------------------------

$("refreshBtn").addEventListener("click", async () => {

    $("refreshBtn").disabled = true;

    await loadAnalytics();

    $("refreshBtn").disabled = false;

});

// ------------------------------
// REDRAW ON RESIZE
// ------------------------------

window.addEventListener("resize", () => {

    if (chartData.labels.length) {
        drawActivityChart();
        drawModeChart();
    }

});

// ------------------------------
// INITIALIZE
// ------------------------------

async function initialize() {

    const loggedIn = await checkUser();

    if (loggedIn) {
        await loadAnalytics();
    }

}

initialize();