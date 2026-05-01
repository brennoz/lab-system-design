package com.lab.webcrawler.domain.model;

import java.time.Instant;
import java.util.List;

// Pattern: Aggregate — immutable snapshot of a fetched page; owned by crawler, persisted after fetch
public record CrawlPage(
        String url,
        String htmlContent,
        List<String> extractedLinks,
        Instant crawledAt
) {
}
