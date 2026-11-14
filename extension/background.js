const API_URL = "http://127.0.0.1:5000/api/history";

// Capture completed page navigation
chrome.webNavigation.onCommitted.addListener(async (details) => {

    // Only track main frames
    if (details.frameId !== 0) return;

    const url = details.url;

    // Ignore non-web URLs
    if (!url.startsWith("http://") &&
        !url.startsWith("https://")) {
        return;
    }

    try {
        // Get tab information
        const tab = await chrome.tabs.get(details.tabId);

        const historyData = {
            url: url,
            title: tab.title || "Unknown",
            browserMode: tab.incognito ? "incognito" : "normal"
        };

        // Send data to local backend
        const response = await fetch(API_URL, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(historyData)
        });

        if (!response.ok) {
            throw new Error(`API error: ${response.status}`);
        }

        console.log("ChronoTrack saved:", historyData);

    } catch (error) {
        console.error("ChronoTrack Error:", error);
    }
});

console.log("ChronoTrack extension started");