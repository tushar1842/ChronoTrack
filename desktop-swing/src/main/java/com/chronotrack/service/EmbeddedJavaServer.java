package com.chronotrack.service;

import com.chronotrack.model.HistoryRecord;
import com.chronotrack.util.JsonHelper;
import com.chronotrack.util.UrlHelper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Built-in embedded Java HTTP server implementing the ChronoTrack REST API.
 * Allows the entire project to run with a pure Java backend, supporting both the
 * Chrome Extension and the Java Swing Desktop Client without requiring Node.js or MongoDB.
 */
public class EmbeddedJavaServer {

    private HttpServer server;
    private final int port;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final List<HistoryRecord> records = new CopyOnWriteArrayList<>();
    private final List<Consumer<HistoryRecord>> onRecordAddedListeners = new CopyOnWriteArrayList<>();
    private File storageFile = new File("chronotrack_storage.json");

    public EmbeddedJavaServer(int port) {
        this.port = port;
        loadFromDisk();
    }

    public synchronized void start() throws IOException {
        if (running.get()) return;

        server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.setExecutor(Executors.newCachedThreadPool());

        // Root health check endpoint
        server.createContext("/", exchange -> {
            addCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            byte[] response = "ChronoTrack Java Server is Running!".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });

        // /api/history endpoint
        server.createContext("/api/history", new HistoryHttpHandler());

        server.start();
        running.set(true);
        System.out.println("ChronoTrack Embedded Java Server started on http://127.0.0.1:" + port);
    }

    public synchronized void stop() {
        if (!running.get() || server == null) return;
        saveToDisk();
        server.stop(1);
        server = null;
        running.set(false);
        System.out.println("ChronoTrack Embedded Java Server stopped.");
    }

    public boolean isRunning() {
        return running.get();
    }

    public int getPort() {
        return port;
    }

    public List<HistoryRecord> getRecords() {
        return Collections.unmodifiableList(new ArrayList<>(records));
    }

    public void addRecord(HistoryRecord record) {
        if (record.getId() == null || record.getId().isBlank()) {
            record.setId(UUID.randomUUID().toString());
        }
        records.add(0, record);
        saveToDisk();
        for (Consumer<HistoryRecord> listener : onRecordAddedListeners) {
            try {
                listener.accept(record);
            } catch (Exception ignored) {}
        }
    }

    public void addRecordListener(Consumer<HistoryRecord> listener) {
        onRecordAddedListeners.add(listener);
    }

    public void removeRecordListener(Consumer<HistoryRecord> listener) {
        onRecordAddedListeners.remove(listener);
    }

    public boolean deleteRecord(String id) {
        boolean removed = records.removeIf(r -> Objects.equals(r.getId(), id));
        if (removed) saveToDisk();
        return removed;
    }

    public void clearAll() {
        records.clear();
        saveToDisk();
    }

    private class HistoryHttpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            String method = exchange.getRequestMethod().toUpperCase();
            String path = exchange.getRequestURI().getPath();

            if ("OPTIONS".equals(method)) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("GET".equals(method)) {
                // Return records list sorted newest first
                String json = JsonHelper.toJsonArray(new ArrayList<>(records));
                sendJsonResponse(exchange, 200, json);
                return;
            }

            if ("POST".equals(method)) {
                try {
                    String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    Map<String, String> map = JsonHelper.parseSimpleObject(body);
                    String url = map.get("url");
                    String title = map.get("title");
                    String browserMode = map.getOrDefault("browserMode", "normal");

                    if (url == null || (!"normal".equalsIgnoreCase(browserMode) && !"incognito".equalsIgnoreCase(browserMode))) {
                        sendJsonResponse(exchange, 400, "{\"message\":\"Invalid history data\"}");
                        return;
                    }

                    HistoryRecord newRec = new HistoryRecord(
                            UUID.randomUUID().toString(),
                            url,
                            UrlHelper.extractDomain(url),
                            (title != null && !title.isBlank()) ? title : "Unknown",
                            browserMode,
                            Instant.now()
                    );

                    addRecord(newRec);

                    Map<String, Object> respMap = new LinkedHashMap<>();
                    respMap.put("message", "History saved");
                    respMap.put("data", newRec);
                    sendJsonResponse(exchange, 201, JsonHelper.mapToJson(respMap));
                } catch (Exception e) {
                    sendJsonResponse(exchange, 500, "{\"message\":\"" + JsonHelper.escape(e.getMessage()) + "\"}");
                }
                return;
            }

            if ("DELETE".equals(method)) {
                // Check if specific id in path /api/history/{id}
                String[] parts = path.split("/");
                if (parts.length > 3) {
                    String id = parts[3];
                    deleteRecord(id);
                    sendJsonResponse(exchange, 200, "{\"message\":\"Record deleted\"}");
                } else {
                    clearAll();
                    sendJsonResponse(exchange, 200, "{\"message\":\"All history cleared\"}");
                }
                return;
            }

            exchange.sendResponseHeaders(405, -1);
        }
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization, Accept");
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private synchronized void saveToDisk() {
        try (FileWriter fw = new FileWriter(storageFile, StandardCharsets.UTF_8)) {
            fw.write(JsonHelper.toJsonArray(new ArrayList<>(records)));
        } catch (Exception ignored) {}
    }

    private synchronized void loadFromDisk() {
        if (!storageFile.exists()) return;
        try (FileReader fr = new FileReader(storageFile, StandardCharsets.UTF_8)) {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[1024];
            int read;
            while ((read = fr.read(buf)) != -1) {
                sb.append(buf, 0, read);
            }
            List<HistoryRecord> loaded = JsonHelper.parseHistoryRecords(sb.toString());
            records.clear();
            records.addAll(loaded);
        } catch (Exception ignored) {}
    }
}
