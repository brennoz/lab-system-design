package com.lab.flight.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

// Aggregate — owns the booking lifecycle; immutable record, status transitions via withStatus()
public record Booking(
        UUID id,
        String flightId,
        String userId,
        String provider,
        BigDecimal price,
        BookingStatus status,
        Instant createdAt
) {
    public static Booking pending(String flightId, String userId, String provider, BigDecimal price) {
        return new Booking(UUID.randomUUID(), flightId, userId, provider, price,
                BookingStatus.PENDING, Instant.now());
    }

    public Booking withStatus(BookingStatus newStatus) {
        return new Booking(id, flightId, userId, provider, price, newStatus, createdAt);
    }
}
