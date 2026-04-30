package com.lab.urlshortener.domain.model;

import java.time.Instant;

// Pattern: Aggregate Root — immutable record; id assigned by DB sequence, never changed after creation
// Why record: all fields are identity-forming; record gives equals/hashCode/toString for free
public record Url(long id, String originalUrl, Instant createdAt) {
}
