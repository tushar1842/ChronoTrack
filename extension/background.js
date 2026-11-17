const API_URL = "http://127.0.0.1:5000/api/history";

// ======================================
// DEFAULT SETTINGS
// ======================================

const DEFAULT_SETTINGS = {
    trackingEnabled: true,
    incognitoTracking: true,
    autoRefresh: true,
    notifications: false
};

// ======================================
// GET USER SETTINGS
// ======================================

async function getSettings() {

    try {

        const settings = await chrome.storage.local.get(
            DEFAULT_SETTINGS
        );

        return settings;

    } catch (error) {

        console.error("Settings Error:", error);

        return DEFAULT_SETTINGS;

    }

}

// ======================================
// INITIALIZE SETTINGS
// ======================================

chrome.runtime.onInstalled.addListener(async () => {

    const existing = await chrome.storage.local.get(
        Object.keys(DEFAULT_SETTINGS)
    );

    const missing = {};

    for (const key in DEFAULT_SETTINGS) {

        if (existing[key] === undefined) {
            missing[key] = DEFAULT_SETTINGS[key];
        }

    }

    if (Object.keys(missing).length > 0) {

        await chrome.storage.local.set(missing);

    }

    console.log("ChronoTrack settings initialized");

});

// ======================================
// TRACK WEBSITE VISITS
// ======================================

chrome.webNavigation.onCommitted.addListener(async (details) => {

    // Only main frame navigation
    if (details.frameId !== 0) return;

    const url = details.url;

    // Only HTTP and HTTPS websites
    if (
        !url.startsWith("http://") &&
        !url.startsWith("https://")
    ) {
        return;
    }

    try {

        // ----------------------------------
        // CHECK SETTINGS
        // ----------------------------------

        const settings = await getSettings();

        // Tracking disabled
        if (!settings.trackingEnabled) {

            console.log("ChronoTrack: Tracking disabled");

            return;

        }

        // ----------------------------------
        // GET TAB INFORMATION
        // ----------------------------------

        const tab = await chrome.tabs.get(details.tabId);

        const isIncognito = tab.incognito;

        // Incognito tracking disabled
        if (
            isIncognito &&
            !settings.incognitoTracking
        ) {

            console.log("ChronoTrack: Incognito tracking disabled");

            return;

        }

        // ----------------------------------
        // PREPARE HISTORY DATA
        // ----------------------------------

        const historyData = {

            url: url,

            title: tab.title || "Unknown",

            browserMode: isIncognito
                ? "incognito"
                : "normal"

        };

        // ----------------------------------
        // SEND DATA TO BACKEND
        // ----------------------------------

        const response = await fetch(API_URL, {

            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify(historyData)

        });

        if (!response.ok) {

            throw new Error(
                `API error: ${response.status}`
            );

        }

        console.log(
            "ChronoTrack saved:",
            historyData
        );

        // ----------------------------------
        // NOTIFICATIONS
        // ----------------------------------

        if (settings.notifications) {

            chrome.notifications.create({

                type: "basic",

                iconUrl: "icon.png",

                title: "ChronoTrack",

                message: "Website visit recorded"

            });

        }

    } catch (error) {

        console.error(
            "ChronoTrack Error:",
            error
        );

    }

});

console.log("ChronoTrack background service started");