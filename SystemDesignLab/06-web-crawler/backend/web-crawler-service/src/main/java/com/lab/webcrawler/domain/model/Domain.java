package com.lab.webcrawler.domain.model;

import java.net.URI;
import java.net.URISyntaxException;

// Pattern: Value Object — extracts and normalises hostname from a URL
// Why separate type: politeness lock key is "crawl:lock:{domain}" — must be consistent regardless of URL path
public record Domain(String value) {

    public static Domain from(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (host == null || host.isBlank()) {
                throw new IllegalArgumentException("Cannot extract domain from: " + url);
            }
            return new Domain(host.toLowerCase());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid URL: " + url, e);
        }
    }
}
