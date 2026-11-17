package com.chronotrack.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Model representing a browsing history entry in ChronoTrack.
 */
public class HistoryRecord {

    private String id;
    private String url;
    private String domain;
    private String title;
    private String browserMode; // "normal" or "incognito"
    private Instant visitTime;

    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public HistoryRecord() {
        this.visitTime = Instant.now();
        this.browserMode = "normal";
    }

    public HistoryRecord(String url, String title, String browserMode) {
        this();
        this.url = url;
        this.title = (title != null && !title.isBlank()) ? title : "Untitled";
        this.browserMode = ("incognito".equalsIgnoreCase(browserMode)) ? "incognito" : "normal";
        this.domain = extractDomain(url);
    }

    public HistoryRecord(String id, String url, String domain, String title, String browserMode, Instant visitTime) {
        this.id = id;
        this.url = url;
        this.domain = (domain != null && !domain.isBlank()) ? domain : extractDomain(url);
        this.title = (title != null && !title.isBlank()) ? title : "Untitled";
        this.browserMode = ("incognito".equalsIgnoreCase(browserMode)) ? "incognito" : "normal";
        this.visitTime = (visitTime != null) ? visitTime : Instant.now();
    }

    private static String extractDomain(String urlString) {
        if (urlString == null || urlString.isBlank()) {
            return "unknown";
        }
        try {
            java.net.URI uri = new java.net.URI(urlString);
            String host = uri.getHost();
            if (host != null) {
                return host.startsWith("www.") ? host.substring(4) : host;
            }
        } catch (Exception ignored) {
        }
        // Fallback regex/string split
        String clean = urlString.replaceFirst("^https?://", "");
        int slash = clean.indexOf('/');
        if (slash > 0) {
            clean = clean.substring(0, slash);
        }
        int colon = clean.indexOf(':');
        if (colon > 0) {
            clean = clean.substring(0, colon);
        }
        return clean.startsWith("www.") ? clean.substring(4) : clean;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        if (this.domain == null || this.domain.isBlank() || "unknown".equals(this.domain)) {
            this.domain = extractDomain(url);
        }
    }

    public String getDomain() {
        return domain != null ? domain : extractDomain(url);
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getTitle() {
        return title != null ? title : "Untitled";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBrowserMode() {
        return browserMode != null ? browserMode.toLowerCase() : "normal";
    }

    public void setBrowserMode(String browserMode) {
        this.browserMode = ("incognito".equalsIgnoreCase(browserMode)) ? "incognito" : "normal";
    }

    public boolean isIncognito() {
        return "incognito".equalsIgnoreCase(browserMode);
    }

    public Instant getVisitTime() {
        return visitTime;
    }

    public void setVisitTime(Instant visitTime) {
        this.visitTime = visitTime;
    }

    public String getFormattedVisitTime() {
        if (visitTime == null) {
            return "-";
        }
        LocalDateTime ldt = LocalDateTime.ofInstant(visitTime, ZoneId.systemDefault());
        return DISPLAY_FORMATTER.format(ldt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HistoryRecord that)) return false;
        return Objects.equals(id, that.id) ||
                (Objects.equals(url, that.url) && Objects.equals(visitTime, that.visitTime));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, url, visitTime);
    }

    @Override
    public String toString() {
        return "HistoryRecord{" +
                "id='" + id + '\'' +
                ", domain='" + domain + '\'' +
                ", title='" + title + '\'' +
                ", browserMode='" + browserMode + '\'' +
                ", visitTime=" + getFormattedVisitTime() +
                '}';
    }
}
