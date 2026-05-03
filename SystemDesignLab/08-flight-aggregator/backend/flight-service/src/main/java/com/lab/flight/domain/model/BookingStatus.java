package com.lab.flight.domain.model;

// State machine: PENDING → HELD → CONFIRMED → COMPLETED | FAILED
public enum BookingStatus {
    PENDING, HELD, CONFIRMED, COMPLETED, FAILED
}
