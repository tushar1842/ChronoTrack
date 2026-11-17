package com.chronotrack.util;

import java.awt.Desktop;
import java.net.URI;

public class UrlHelper {

    private UrlHelper() {}

    /**
     * Extracts host domain from a URL string (stripping www. if present).
     */
    public static String extractDomain(String url) {
        if (url == null || url.isBlank()) {
            return "unknown";
        }
        try {
            URI uri = new URI(url.trim());
            String host = uri.getHost();
            if (host != null) {
                return host.startsWith("www.") ? host.substring(4) : host;
            }
        } catch (Exception ignored) {}

        String clean = url.trim().replaceFirst("^https?://", "");
        int slash = clean.indexOf('/');
        if (slash > 0) clean = clean.substring(0, slash);
        int colon = clean.indexOf(':');
        if (colon > 0) clean = clean.substring(0, colon);
        return clean.startsWith("www.") ? clean.substring(4) : clean;
    }

    /**
     * Truncate string with ellipsis if exceeds maxLength.
     */
    public static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    /**
     * Opens the given URL in the user's default system browser.
     */
    public static boolean openInBrowser(String url) {
        if (url == null || url.isBlank()) return false;
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                return true;
            }
        } catch (Exception e) {
            System.err.println("Failed to open URL in browser: " + e.getMessage());
        }
        return false;
    }

    /**
     * Checks if string is a valid HTTP/HTTPS URL.
     */
    public static boolean isValidHttpUrl(String url) {
        if (url == null || url.isBlank()) return false;
        String lower = url.trim().toLowerCase();
        return lower.startsWith("http://") || lower.startsWith("https://");
    }
}
