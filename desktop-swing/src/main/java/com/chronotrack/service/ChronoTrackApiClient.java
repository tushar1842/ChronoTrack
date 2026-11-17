package com.chronotrack.service;

import com.chronotrack.model.HistoryRecord;
import com.chronotrack.util.JsonHelper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API client that interacts with the ChronoTrack backend (Node.js or Java server).
 */
public class ChronoTrackApiClient {

    private final HttpClient httpClient;
    private final Duration timeout;

    public ChronoTrackApiClient() {
        this(Duration.ofSeconds(4));
    }

    public ChronoTrackApiClient(Duration timeout) {
        this.timeout = timeout;
        HttpClient client = null;
        try {
            client = HttpClient.newBuilder()
                    .connectTimeout(timeout)
                    .build();
        } catch (Throwable t) {
            System.err.println("HttpClient socket warning: " + t.getMessage());
        }
        this.httpClient = client;
    }

    /**
     * Checks if backend is reachable at the given base URL.
     */
    public boolean testConnection(String baseUrl) {
        if (httpClient == null) return false;
        try {
            String url = sanitizeUrl(baseUrl);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url + "/api/history"))
                    .timeout(timeout)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() >= 200 && response.statusCode() < 300;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Fetches all browsing history records from GET /api/history.
     */
    public List<HistoryRecord> fetchHistory(String baseUrl) throws IOException, InterruptedException {
        if (httpClient == null) throw new IOException("HTTP client unavailable (no network socket permission)");
        String url = sanitizeUrl(baseUrl) + "/api/history";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .timeout(timeout)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("HTTP error: " + response.statusCode() + " - " + response.body());
        }

        return JsonHelper.parseHistoryRecords(response.body());
    }

    /**
     * Posts a new browsing history entry to POST /api/history.
     */
    public HistoryRecord saveRecord(String baseUrl, HistoryRecord record) throws IOException, InterruptedException {
        if (httpClient == null) throw new IOException("HTTP client unavailable (no network socket permission)");
        String url = sanitizeUrl(baseUrl) + "/api/history";

        Map<String, Object> payload = new HashMap<>();
        payload.put("url", record.getUrl());
        payload.put("title", record.getTitle());
        payload.put("browserMode", record.getBrowserMode());

        String jsonPayload = JsonHelper.mapToJson(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .timeout(timeout)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200 && response.statusCode() != 201) {
            throw new IOException("Failed to save history: HTTP " + response.statusCode());
        }

        HistoryRecord parsed = JsonHelper.parseSingleRecord(response.body());
        return parsed != null ? parsed : record;
    }

    /**
     * Deletes a record from DELETE /api/history/:id.
     */
    public boolean deleteRecord(String baseUrl, String id) {
        if (httpClient == null || id == null || id.isBlank()) return false;
        try {
            String url = sanitizeUrl(baseUrl) + "/api/history/" + id;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(timeout)
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() >= 200 && response.statusCode() < 300;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Clears all history from DELETE /api/history.
     */
    public boolean clearAll(String baseUrl) {
        if (httpClient == null) return false;
        try {
            String url = sanitizeUrl(baseUrl) + "/api/history";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(timeout)
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() >= 200 && response.statusCode() < 300;
        } catch (Exception e) {
            return false;
        }
    }

    private String sanitizeUrl(String url) {
        if (url == null || url.isBlank()) return "http://127.0.0.1:5000";
        String s = url.trim();
        if (s.endsWith("/")) s = s.substring(0, s.length() - 1);
        if (!s.startsWith("http://") && !s.startsWith("https://")) {
            s = "http://" + s;
        }
        return s;
    }
}
