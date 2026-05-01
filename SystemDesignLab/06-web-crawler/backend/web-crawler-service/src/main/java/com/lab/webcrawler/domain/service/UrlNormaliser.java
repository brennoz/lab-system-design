package com.lab.webcrawler.domain.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// Algorithm: URL normalisation — collapses multiple URL forms of same page to one canonical string
// Why: prevents Bloom Filter misses on "bbc.com/news#comments" vs "bbc.com/news" (same page, different string)
public class UrlNormaliser {

    private static final List<String> ALLOWED_SCHEMES = List.of("http", "https");
    // Why strip these: marketing tracking params create infinite URL variants of the same page (crawler trap)
    private static final List<String> TRACKING_PARAMS =
            List.of("utm_source", "utm_medium", "utm_campaign", "utm_term", "utm_content", "fbclid", "gclid");

    public Optional<String> normalise(String rawUrl) {
        try {
            URI uri = new URI(rawUrl);
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase();

            if (!ALLOWED_SCHEMES.contains(scheme)) {
                return Optional.empty();
            }

            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase();
            String cleanQuery = stripTrackingParams(uri.getQuery());

            // null fragment: strip — fragment is client-side only, never changes server response
            URI normalised = new URI(scheme, null, host, uri.getPort(), uri.getPath(), cleanQuery, null);
            return Optional.of(normalised.toString());
        } catch (URISyntaxException e) {
            return Optional.empty();
        }
    }

    private String stripTrackingParams(String query) {
        if (query == null || query.isBlank()) return null;

        String cleaned = Arrays.stream(query.split("&"))
                .filter(param -> {
                    String key = param.split("=")[0].toLowerCase();
                    return TRACKING_PARAMS.stream().noneMatch(key::startsWith);
                })
                .collect(Collectors.joining("&"));

        return cleaned.isBlank() ? null : cleaned;
    }
}
