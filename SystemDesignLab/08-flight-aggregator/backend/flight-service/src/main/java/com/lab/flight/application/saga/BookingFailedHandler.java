package com.lab.flight.application.saga;

import com.lab.flight.domain.model.BookingStatus;
import com.lab.flight.domain.port.BookingRepository;

import java.util.UUID;

// Pattern: Choreography Saga compensation — BOOKING_FAILED → DB:FAILED; no provider rollback
public class BookingFailedHandler {

    private final BookingRepository bookingRepository;

    public BookingFailedHandler(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public void handle(String bookingId) {
        bookingRepository.updateStatus(UUID.fromString(bookingId), BookingStatus.FAILED);
    }
}
