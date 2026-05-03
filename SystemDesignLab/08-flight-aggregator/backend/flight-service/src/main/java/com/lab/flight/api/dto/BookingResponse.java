package com.lab.flight.api.dto;

import com.lab.flight.domain.model.BookingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BookingResponse(UUID id, String flightId, String userId, String provider,
                               BigDecimal price, BookingStatus status, Instant createdAt) {}
