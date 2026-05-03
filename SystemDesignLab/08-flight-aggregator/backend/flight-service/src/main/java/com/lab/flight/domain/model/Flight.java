package com.lab.flight.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

// Value Object — immutable snapshot of one flight from one provider
public record Flight(
        String flightId,
        String origin,
        String destination,
        Instant departureTime,
        BigDecimal price,
        String provider
) {}
