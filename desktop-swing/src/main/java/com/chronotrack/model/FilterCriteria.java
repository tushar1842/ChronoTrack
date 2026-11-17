package com.chronotrack.model;

import java.util.Objects;

/**
 * Filter and search criteria for HistoryRecords.
 */
public class FilterCriteria {

    private String searchKeyword;
    private String mode; // "all", "normal", "incognito"
    private String domain;

    public FilterCriteria() {
        this.searchKeyword = "";
        this.mode = "all";
        this.domain = "all";
    }

    public FilterCriteria(String searchKeyword, String mode, String domain) {
        this.searchKeyword = searchKeyword != null ? searchKeyword.trim().toLowerCase() : "";
        this.mode = mode != null ? mode.trim().toLowerCase() : "all";
        this.domain = domain != null ? domain.trim().toLowerCase() : "all";
    }

    public boolean matches(HistoryRecord record) {
        if (record == null) return false;

        // Mode check
        if (!"all".equalsIgnoreCase(mode) && !mode.isBlank()) {
            if (!mode.equalsIgnoreCase(record.getBrowserMode())) {
                return false;
            }
        }

        // Domain check
        if (!"all".equalsIgnoreCase(domain) && !domain.isBlank()) {
            if (!domain.equalsIgnoreCase(record.getDomain())) {
                return false;
            }
        }

        // Search keyword check across title, url, domain
        if (!searchKeyword.isBlank()) {
            String kw = searchKeyword.toLowerCase();
            boolean matchTitle = record.getTitle() != null && record.getTitle().toLowerCase().contains(kw);
            boolean matchUrl = record.getUrl() != null && record.getUrl().toLowerCase().contains(kw);
            boolean matchDomain = record.getDomain() != null && record.getDomain().toLowerCase().contains(kw);
            return matchTitle || matchUrl || matchDomain;
        }

        return true;
    }

    public String getSearchKeyword() {
        return searchKeyword;
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword != null ? searchKeyword.trim().toLowerCase() : "";
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode != null ? mode.trim().toLowerCase() : "all";
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain != null ? domain.trim().toLowerCase() : "all";
    }
}
