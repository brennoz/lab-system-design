package com.lab.urlshortener.api.dto;

// Pattern: DTO — response payload; shortCode is Base62 value, shortUrl is the full clickable link
public record ShortenResponse(String shortCode, String shortUrl) {
}
