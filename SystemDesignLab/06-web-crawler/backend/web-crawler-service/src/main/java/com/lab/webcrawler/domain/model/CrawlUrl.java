package com.lab.webcrawler.domain.model;

// Pattern: Value Object — immutable; carries URL + depth together to prevent primitive obsession
// Why depth: BFS needs depth to enforce MAX_DEPTH limit and prevent crawler trap recursion
public record CrawlUrl(String url, int depth) {

    public CrawlUrl {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("CrawlUrl: url cannot be blank");
        }
        if (depth < 0) {
            throw new IllegalArgumentException("CrawlUrl: depth cannot be negative");
        }
    }
}
