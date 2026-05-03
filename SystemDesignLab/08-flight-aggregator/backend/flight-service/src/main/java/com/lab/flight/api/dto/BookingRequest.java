package com.lab.flight.api.dto;

import java.math.BigDecimal;

public record BookingRequest(String flightId, String userId, String provider, BigDecimal price) {}
