package com.lab.flight.domain.model;

import java.time.LocalDate;

// Value Object — search parameters; cacheKey() is the Redis key for Cache-Aside
public record SearchQuery(String origin, String destination, LocalDate date) {

    public String cacheKey() {
        return origin + ":" + destination + ":" + date;
    }
}
