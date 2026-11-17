// ======================================
// CHRONOTRACK SETTINGS
// ======================================

const DEFAULT_SETTINGS = {

    trackingEnabled: true,

    incognitoTracking: true,

    autoRefresh: true,

    notifications: false

};

const pageRoutes = {

    dashboard: "dashboard.html",

    history: "history.html",

    analytics: "analytics.html",

    settings: "settings.html"

};

// ======================================
// ELEMENTS
// ======================================

const trackingToggle =
    document.getElementById("trackingEnabled");

const incognitoToggle =
    document.getElementById("incognitoTracking");

const refreshToggle =
    document.getElementById("autoRefresh");

const notificationToggle =
    document.getElementById("notifications");

const statusMessage =
    document.getElementById("statusMessage");

let statusTimer;

// ======================================
// SHOW STATUS
// ======================================

function showStatus(message) {

    statusMessage.textContent = message;

    statusMessage.classList.add("show");

    clearTimeout(statusTimer);

    statusTimer = setTimeout(() => {

        statusMessage.classList.remove("show");

    }, 2500);

}

// ======================================
// CHECK LOGIN
// ======================================

async function loadUserProfile() {

    const data = await chrome.storage.local.get(
        "chronotrackUser"
    );

    const user = data.chronotrackUser;

    if (!user) {

        window.location.href = "login.html";

        return false;

    }

    document.getElementById("userName").textContent =
        user.name || "User";

    document.getElementById("userEmail").textContent =
        user.email || "";

    const image = document.getElementById("userImage");

    if (user.picture) {

        image.src = user.picture;

    } else {

        image.style.display = "none";

    }

    return true;

}

// ======================================
// LOAD SETTINGS
// ======================================

async function loadSettings() {

    try {

        const settings = await chrome.storage.local.get(
            DEFAULT_SETTINGS
        );

        trackingToggle.checked =
            settings.trackingEnabled;

        incognitoToggle.checked =
            settings.incognitoTracking;

        refreshToggle.checked =
            settings.autoRefresh;

        notificationToggle.checked =
            settings.notifications;

        // Disable Incognito control when master tracking is OFF

        incognitoToggle.disabled =
            !settings.trackingEnabled;

        console.log("Settings loaded:", settings);

    } catch (error) {

        console.error("Settings loading error:", error);

        showStatus("Unable to load settings.");

    }

}

// ======================================
// SAVE SETTING
// ======================================

async function saveSetting(key, value) {

    try {

        await chrome.storage.local.set({

            [key]: value

        });

        showStatus("Settings saved successfully.");

        console.log(
            "Setting updated:",
            key,
            value
        );

    } catch (error) {

        console.error("Settings save error:", error);

        showStatus("Failed to save setting.");

    }

}

// ======================================
// MASTER TRACKING
// ======================================

trackingToggle.addEventListener("change", async () => {

    const enabled = trackingToggle.checked;

    await saveSetting(
        "trackingEnabled",
        enabled
    );

    incognitoToggle.disabled = !enabled;

});

// ======================================
// INCOGNITO TRACKING
// ======================================

incognitoToggle.addEventListener("change", async () => {

    await saveSetting(
        "incognitoTracking",
        incognitoToggle.checked
    );

});

// ======================================
// AUTO REFRESH
// ======================================

refreshToggle.addEventListener("change", async () => {

    await saveSetting(
        "autoRefresh",
        refreshToggle.checked
    );

});

// ======================================
// NOTIFICATIONS
// ======================================

notificationToggle.addEventListener("change", async () => {

    await saveSetting(
        "notifications",
        notificationToggle.checked
    );

});

// ======================================
// RESET SETTINGS
// ======================================

document.getElementById("resetSettings")
    .addEventListener("click", async () => {

        const confirmed = confirm(
            "Are you sure you want to reset all settings?"
        );

        if (!confirmed) return;

        try {

            await chrome.storage.local.set(
                DEFAULT_SETTINGS
            );

            await loadSettings();

            showStatus(
                "All settings restored to defaults."
            );

        } catch (error) {

            console.error("Reset error:", error);

            showStatus("Unable to reset settings.");

        }

    });

// ======================================
// SIDEBAR NAVIGATION
// ======================================

document.querySelectorAll(".menu-item[data-page]")
    .forEach(item => {

        item.addEventListener("click", () => {

            const selectedPage = item.dataset.page;

            const target = pageRoutes[selectedPage];

            if (target) {

                window.location.href = target;

            }

        });

    });

// ======================================
// LOGOUT
// ======================================

document.getElementById("logout")
    .addEventListener("click", async () => {

        await chrome.storage.local.remove([

            "chronotrackUser",

            "googleAccessToken"

        ]);

        window.location.href = "login.html";

    });

// ======================================
// INITIALIZE
// ======================================

async function initializeSettings() {

    const loggedIn = await loadUserProfile();

    if (!loggedIn) return;

    await loadSettings();

}

initializeSettings();