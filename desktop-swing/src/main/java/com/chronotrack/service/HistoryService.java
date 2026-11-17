package com.chronotrack.service;

import com.chronotrack.model.FilterCriteria;
import com.chronotrack.model.HistoryRecord;
import com.chronotrack.util.JsonHelper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Service managing history records, statistics, filtering, export, and listeners.
 */
public class HistoryService {

    public interface HistoryChangeListener {
        void onHistoryUpdated(List<HistoryRecord> records);
    }

    private ChronoTrackApiClient apiClient;
    private EmbeddedJavaServer embeddedServer;
    private String apiBaseUrl = "http://127.0.0.1:5000";
    private boolean useEmbeddedServer = false;

    private final List<HistoryRecord> currentRecords = new CopyOnWriteArrayList<>();
    private final List<HistoryChangeListener> listeners = new CopyOnWriteArrayList<>();

    public HistoryService() {
        this.apiClient = new ChronoTrackApiClient();
        try {
            this.embeddedServer = new EmbeddedJavaServer(5000);
            this.embeddedServer.addRecordListener(record -> {
                if (useEmbeddedServer) {
                    currentRecords.add(0, record);
                    notifyListeners();
                }
            });
        } catch (Throwable t) {
            System.err.println("Embedded server initialization notice: " + t.getMessage());
        }
    }

    public void addListener(HistoryChangeListener listener) {
        listeners.add(listener);
    }

    public void removeListener(HistoryChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        List<HistoryRecord> unmodifiable = Collections.unmodifiableList(new ArrayList<>(currentRecords));
        for (HistoryChangeListener l : listeners) {
            try {
                l.onHistoryUpdated(unmodifiable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Refreshes history from either the active HTTP backend or embedded server.
     */
    public synchronized boolean refreshHistory() {
        if (useEmbeddedServer && embeddedServer != null && embeddedServer.isRunning()) {
            currentRecords.clear();
            currentRecords.addAll(embeddedServer.getRecords());
            notifyListeners();
            return true;
        }

        try {
            List<HistoryRecord> fetched = getApiClient().fetchHistory(apiBaseUrl);
            currentRecords.clear();
            currentRecords.addAll(fetched);
            notifyListeners();
            return true;
        } catch (Exception e) {
            // Keep local records intact if backend is not reachable
            return false;
        }
    }

    /**
     * Adds a new history record.
     */
    public boolean addRecord(HistoryRecord record) {
        if (useEmbeddedServer && embeddedServer != null && embeddedServer.isRunning()) {
            embeddedServer.addRecord(record);
            return true;
        }

        try {
            HistoryRecord saved = getApiClient().saveRecord(apiBaseUrl, record);
            currentRecords.add(0, saved);
            notifyListeners();
            return true;
        } catch (Exception e) {
            // Fallback: save locally in list
            currentRecords.add(0, record);
            notifyListeners();
            return false;
        }
    }

    /**
     * Deletes a record by ID.
     */
    public boolean deleteRecord(String id) {
        if (useEmbeddedServer && embeddedServer != null && embeddedServer.isRunning()) {
            boolean removed = embeddedServer.deleteRecord(id);
            if (removed) {
                currentRecords.removeIf(r -> Objects.equals(r.getId(), id));
                notifyListeners();
            }
            return removed;
        }

        boolean ok = getApiClient().deleteRecord(apiBaseUrl, id);
        currentRecords.removeIf(r -> Objects.equals(r.getId(), id));
        notifyListeners();
        return ok;
    }

    /**
     * Clears all history records.
     */
    public boolean clearAll() {
        if (useEmbeddedServer && embeddedServer != null && embeddedServer.isRunning()) {
            embeddedServer.clearAll();
            currentRecords.clear();
            notifyListeners();
            return true;
        }

        boolean ok = getApiClient().clearAll(apiBaseUrl);
        currentRecords.clear();
        notifyListeners();
        return ok;
    }

    public List<HistoryRecord> getAllRecords() {
        return Collections.unmodifiableList(new ArrayList<>(currentRecords));
    }

    public List<HistoryRecord> getFilteredRecords(FilterCriteria criteria) {
        if (criteria == null) return getAllRecords();
        return currentRecords.stream()
                .filter(criteria::matches)
                .collect(Collectors.toList());
    }

    // --- Statistics ---

    public int getTotalCount() {
        return currentRecords.size();
    }

    public int getNormalCount() {
        return (int) currentRecords.stream().filter(r -> "normal".equalsIgnoreCase(r.getBrowserMode())).count();
    }

    public int getIncognitoCount() {
        return (int) currentRecords.stream().filter(r -> "incognito".equalsIgnoreCase(r.getBrowserMode())).count();
    }

    public int getUniqueDomainsCount() {
        return (int) currentRecords.stream()
                .map(HistoryRecord::getDomain)
                .filter(Objects::nonNull)
                .distinct()
                .count();
    }

    public List<String> getAllUniqueDomains() {
        return currentRecords.stream()
                .map(HistoryRecord::getDomain)
                .filter(d -> d != null && !d.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public Map<String, Integer> getTopDomains(int limit) {
        Map<String, Integer> counts = new HashMap<>();
        for (HistoryRecord r : currentRecords) {
            String domain = r.getDomain();
            if (domain != null && !domain.isBlank()) {
                counts.put(domain, counts.getOrDefault(domain, 0) + 1);
            }
        }

        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    // --- Export Utilities ---

    public void exportToCsv(File file) throws IOException {
        try (FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8)) {
            fw.write("ID,Domain,Mode,Title,URL,VisitTime\n");
            for (HistoryRecord r : currentRecords) {
                fw.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        escapeCsv(r.getId()),
                        escapeCsv(r.getDomain()),
                        escapeCsv(r.getBrowserMode()),
                        escapeCsv(r.getTitle()),
                        escapeCsv(r.getUrl()),
                        escapeCsv(r.getFormattedVisitTime())
                ));
            }
        }
    }

    public void exportToJson(File file) throws IOException {
        try (FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8)) {
            fw.write(JsonHelper.toJsonArray(new ArrayList<>(currentRecords)));
        }
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        return val.replace("\"", "\"\"");
    }

    // --- Demo Data Generator ---

    public void generateSampleData() {
        List<HistoryRecord> samples = List.of(
                new HistoryRecord(UUID.randomUUID().toString(), "https://github.com/explore", "github.com", "Explore GitHub · Trending Repositories", "normal", Instant.now().minus(2, ChronoUnit.MINUTES)),
                new HistoryRecord(UUID.randomUUID().toString(), "https://stackoverflow.com/questions/tagged/java", "stackoverflow.com", "Newest 'java' Questions - Stack Overflow", "normal", Instant.now().minus(8, ChronoUnit.MINUTES)),
                new HistoryRecord(UUID.randomUUID().toString(), "https://en.wikipedia.org/wiki/Java_Swing", "wikipedia.org", "Swing (Java) - Wikipedia", "incognito", Instant.now().minus(15, ChronoUnit.MINUTES)),
                new HistoryRecord(UUID.randomUUID().toString(), "https://docs.oracle.com/en/java/", "oracle.com", "Java SE Documentation - Official Portal", "normal", Instant.now().minus(25, ChronoUnit.MINUTES)),
                new HistoryRecord(UUID.randomUUID().toString(), "https://www.youtube.com/watch?v=demo", "youtube.com", "Java Swing Full Course Tutorial", "normal", Instant.now().minus(42, ChronoUnit.MINUTES)),
                new HistoryRecord(UUID.randomUUID().toString(), "https://reddit.com/r/java", "reddit.com", "Java Programming Community and News", "incognito", Instant.now().minus(60, ChronoUnit.MINUTES)),
                new HistoryRecord(UUID.randomUUID().toString(), "https://github.com/trending", "github.com", "Trending repositories on GitHub today", "normal", Instant.now().minus(90, ChronoUnit.MINUTES)),
                new HistoryRecord(UUID.randomUUID().toString(), "https://google.com/search?q=chronotrack", "google.com", "ChronoTrack Browser History - Google Search", "normal", Instant.now().minus(110, ChronoUnit.MINUTES)),
                new HistoryRecord(UUID.randomUUID().toString(), "https://developer.mozilla.org/en-US/", "mozilla.org", "MDN Web Docs for Developers", "normal", Instant.now().minus(140, ChronoUnit.MINUTES)),
                new HistoryRecord(UUID.randomUUID().toString(), "https://news.ycombinator.com/", "ycombinator.com", "Hacker News", "incognito", Instant.now().minus(180, ChronoUnit.MINUTES))
        );

        for (HistoryRecord r : samples) {
            addRecord(r);
        }
    }

    // --- Getters / Setters ---

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    public boolean isUseEmbeddedServer() {
        return useEmbeddedServer;
    }

    public void setUseEmbeddedServer(boolean useEmbeddedServer) {
        this.useEmbeddedServer = useEmbeddedServer;
    }

    public EmbeddedJavaServer getEmbeddedServer() {
        if (embeddedServer == null) {
            try {
                embeddedServer = new EmbeddedJavaServer(5000);
            } catch (Throwable t) {
                System.err.println("Could not create embedded server: " + t.getMessage());
            }
        }
        return embeddedServer;
    }

    public ChronoTrackApiClient getApiClient() {
        if (apiClient == null) {
            apiClient = new ChronoTrackApiClient();
        }
        return apiClient;
    }
}
