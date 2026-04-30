package com.lab.urlshortener.api.dto;

import jakarta.validation.constraints.NotBlank;

// Pattern: DTO — carries only what the API boundary needs; no domain objects exposed to clients
public record ShortenRequest(@NotBlank String originalUrl) {
}
