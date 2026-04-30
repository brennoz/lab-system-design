package com.lab.urlshortener.domain.model;

// Pattern: Value Object — immutable, equality by value; prevents primitive obsession on raw String codes
public record ShortCode(String value) {

    public ShortCode {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ShortCode cannot be blank");
        }
    }
}
