package com.chronotrack.util;

import com.chronotrack.model.HistoryRecord;

import java.time.Instant;
import java.util.*;

/**
 * Lightweight, zero-dependency JSON helper designed specifically for ChronoTrack
 * payloads, supporting parsing and serialization of HistoryRecord objects and maps.
 */
public class JsonHelper {

    private JsonHelper() {}

    /**
     * Serializes a HistoryRecord to JSON.
     */
    public static String toJson(HistoryRecord r) {
        StringBuilder sb = new StringBuilder("{");
        appendField(sb, "id", r.getId(), true);
        appendField(sb, "url", r.getUrl(), true);
        appendField(sb, "domain", r.getDomain(), true);
        appendField(sb, "title", r.getTitle(), true);
        appendField(sb, "browserMode", r.getBrowserMode(), true);
        if (r.getVisitTime() != null) {
            appendField(sb, "visitTime", r.getVisitTime().toString(), false);
        } else {
            appendField(sb, "visitTime", Instant.now().toString(), false);
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Serializes a list of HistoryRecords to a JSON array.
     */
    public static String toJsonArray(List<HistoryRecord> records) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < records.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(toJson(records.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Serializes a key-value map to JSON.
     */
    public static String mapToJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        int count = 0;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (count > 0) sb.append(",");
            sb.append("\"").append(escape(entry.getKey())).append("\":");
            Object val = entry.getValue();
            if (val == null) {
                sb.append("null");
            } else if (val instanceof String) {
                sb.append("\"").append(escape((String) val)).append("\"");
            } else if (val instanceof Number || val instanceof Boolean) {
                sb.append(val);
            } else if (val instanceof HistoryRecord) {
                sb.append(toJson((HistoryRecord) val));
            } else {
                sb.append("\"").append(escape(val.toString())).append("\"");
            }
            count++;
        }
        sb.append("}");
        return sb.toString();
    }

    private static void appendField(StringBuilder sb, String key, String value, boolean trailingComma) {
        sb.append("\"").append(escape(key)).append("\":");
        if (value == null) {
            sb.append("null");
        } else {
            sb.append("\"").append(escape(value)).append("\"");
        }
        if (trailingComma) sb.append(",");
    }

    public static String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < ' ') {
                        String hex = String.format("\\u%04x", (int) c);
                        sb.append(hex);
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * Parses a JSON string representing an array of history objects or a wrapper object.
     */
    public static List<HistoryRecord> parseHistoryRecords(String json) {
        List<HistoryRecord> result = new ArrayList<>();
        if (json == null || json.isBlank()) return result;

        String trimmed = json.trim();
        // If wrapped in an object like { "data": [...] }
        if (trimmed.startsWith("{")) {
            int dataIdx = trimmed.indexOf("\"data\"");
            if (dataIdx != -1) {
                int arrayStart = trimmed.indexOf('[', dataIdx);
                if (arrayStart != -1) {
                    trimmed = trimmed.substring(arrayStart);
                }
            }
        }

        if (!trimmed.startsWith("[")) {
            // Check if it's a single object
            if (trimmed.startsWith("{")) {
                HistoryRecord rec = parseSingleRecord(trimmed);
                if (rec != null) result.add(rec);
            }
            return result;
        }

        // Split objects inside array
        List<String> rawObjects = splitJsonArray(trimmed);
        for (String raw : rawObjects) {
            HistoryRecord record = parseSingleRecord(raw);
            if (record != null) {
                result.add(record);
            }
        }

        return result;
    }

    /**
     * Splits top-level objects in a JSON array.
     */
    private static List<String> splitJsonArray(String jsonArray) {
        List<String> items = new ArrayList<>();
        int depth = 0;
        int start = -1;
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < jsonArray.length(); i++) {
            char c = jsonArray.charAt(i);

            if (escaped) {
                escaped = false;
                continue;
            }

            if (c == '\\') {
                escaped = true;
                continue;
            }

            if (c == '"') {
                inString = !inString;
                continue;
            }

            if (inString) continue;

            if (c == '{') {
                if (depth == 0) {
                    start = i;
                }
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start != -1) {
                    items.add(jsonArray.substring(start, i + 1));
                    start = -1;
                }
            }
        }

        return items;
    }

    /**
     * Parses a single JSON object string into a HistoryRecord.
     */
    public static HistoryRecord parseSingleRecord(String jsonObject) {
        Map<String, String> map = parseSimpleObject(jsonObject);
        if (map.isEmpty()) return null;

        String id = map.getOrDefault("_id", map.get("id"));
        String url = map.get("url");
        String domain = map.get("domain");
        String title = map.get("title");
        String browserMode = map.getOrDefault("browserMode", "normal");
        String visitTimeStr = map.get("visitTime");

        if (url == null && title == null) {
            return null;
        }

        Instant visitTime = null;
        if (visitTimeStr != null && !visitTimeStr.isBlank()) {
            try {
                visitTime = Instant.parse(visitTimeStr);
            } catch (Exception e1) {
                try {
                    // Try parsing as timestamp millis
                    long millis = Long.parseLong(visitTimeStr);
                    visitTime = Instant.ofEpochMilli(millis);
                } catch (Exception ignored) {}
            }
        }
        if (visitTime == null) {
            visitTime = Instant.now();
        }

        return new HistoryRecord(id, url, domain, title, browserMode, visitTime);
    }

    /**
     * Parses key-values from a flat or simple JSON object string.
     */
    public static Map<String, String> parseSimpleObject(String json) {
        Map<String, String> map = new LinkedHashMap<>();
        if (json == null || json.isBlank()) return map;

        int i = 0;
        int len = json.length();

        while (i < len) {
            // Find key
            int keyStartQuote = json.indexOf('"', i);
            if (keyStartQuote == -1) break;

            int keyEndQuote = findMatchingQuote(json, keyStartQuote);
            if (keyEndQuote == -1) break;

            String key = unescape(json.substring(keyStartQuote + 1, keyEndQuote));

            // Find colon
            int colon = json.indexOf(':', keyEndQuote);
            if (colon == -1) break;

            // Find value
            int valStart = colon + 1;
            while (valStart < len && Character.isWhitespace(json.charAt(valStart))) {
                valStart++;
            }
            if (valStart >= len) break;

            char firstValChar = json.charAt(valStart);
            String value;
            int nextIndex;

            if (firstValChar == '"') {
                int valEndQuote = findMatchingQuote(json, valStart);
                if (valEndQuote == -1) break;
                value = unescape(json.substring(valStart + 1, valEndQuote));
                nextIndex = valEndQuote + 1;
            } else if (firstValChar == '{') {
                int endObj = findClosingBracket(json, valStart, '{', '}');
                value = (endObj != -1) ? json.substring(valStart, endObj + 1) : "";
                nextIndex = (endObj != -1) ? endObj + 1 : len;
            } else if (firstValChar == '[') {
                int endArr = findClosingBracket(json, valStart, '[', ']');
                value = (endArr != -1) ? json.substring(valStart, endArr + 1) : "";
                nextIndex = (endArr != -1) ? endArr + 1 : len;
            } else {
                // Primitive: number, boolean, null
                int commaOrBrace = findNextDelimiter(json, valStart);
                value = json.substring(valStart, commaOrBrace).trim();
                nextIndex = commaOrBrace;
            }

            map.put(key, value);
            i = nextIndex;
        }

        return map;
    }

    private static int findMatchingQuote(String s, int openQuote) {
        boolean escaped = false;
        for (int i = openQuote + 1; i < s.length(); i++) {
            char c = s.charAt(i);
            if (escaped) {
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                return i;
            }
        }
        return -1;
    }

    private static int findClosingBracket(String s, int openIdx, char openChar, char closeChar) {
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int i = openIdx; i < s.length(); i++) {
            char c = s.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                continue;
            }
            if (c == '"') {
                inString = !inString;
                continue;
            }
            if (!inString) {
                if (c == openChar) depth++;
                else if (c == closeChar) {
                    depth--;
                    if (depth == 0) return i;
                }
            }
        }
        return -1;
    }

    private static int findNextDelimiter(String s, int start) {
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == ',' || c == '}' || c == ']') {
                return i;
            }
        }
        return s.length();
    }

    public static String unescape(String s) {
        if (s == null || !s.contains("\\")) return s;
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(++i);
                switch (next) {
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case '/' -> sb.append('/');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'u' -> {
                        if (i + 4 < s.length()) {
                            String hex = s.substring(i + 1, i + 5);
                            try {
                                sb.append((char) Integer.parseInt(hex, 16));
                                i += 4;
                            } catch (NumberFormatException ignored) {
                                sb.append("\\u");
                            }
                        } else {
                            sb.append("\\u");
                        }
                    }
                    default -> sb.append(next);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
