const loginButton = document.getElementById("googleLogin");
const statusText = document.getElementById("status");

loginButton.addEventListener("click", async () => {
  try {
    loginButton.disabled = true;
    statusText.textContent = "Connecting to Google...";

    // Google Authentication
    const response = await chrome.identity.getAuthToken({
      interactive: true,
    });

    const accessToken = response.token;

    if (!accessToken) {
      throw new Error("Google authentication failed.");
    }

    statusText.textContent = "Fetching account details...";

    // Get Google User Information
    const result = await fetch(
      "https://www.googleapis.com/oauth2/v3/userinfo",
      {
        headers: {
          Authorization: `Bearer ${accessToken}`,
        },
      },
    );

    if (!result.ok) {
      throw new Error("Unable to fetch Google account details.");
    }

    const user = await result.json();

    if (!user.email || !user.sub) {
      throw new Error("Invalid Google account information.");
    }

    // Store basic profile information
    await chrome.storage.local.set({
      chronotrackUser: {
        id: user.sub,
        name: user.name,
        email: user.email,
        picture: user.picture,
      },
    });

    statusText.textContent = "Login successful!";

    // Open Dashboard
    chrome.tabs.create({
      url: chrome.runtime.getURL("dashboard.html"),
    });

    window.close();
  } catch (error) {
    console.error("Login Error:", error);

    statusText.textContent = error.message || "Something went wrong.";

    loginButton.disabled = false;
  }
});
