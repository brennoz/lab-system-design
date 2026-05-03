package com.lab.flight.application.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lab.flight.domain.model.Booking;
import com.lab.flight.domain.model.OutboxEvent;
import com.lab.flight.domain.port.BookingRepository;
import com.lab.flight.domain.port.OutboxPort;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;

// Pattern: Outbox — @Transactional ensures booking + outbox event persist atomically; no dual-write risk
public class CreateBookingUseCase {

    private final BookingRepository bookingRepository;
    private final OutboxPort outboxPort;
    private final ObjectMapper mapper;

    public CreateBookingUseCase(BookingRepository bookingRepository, OutboxPort outboxPort,
                                ObjectMapper mapper) {
        this.bookingRepository = bookingRepository;
        this.outboxPort = outboxPort;
        this.mapper = mapper;
    }

    @Transactional
    public Booking create(String flightId, String userId, String provider, BigDecimal price) {
        Booking booking = Booking.pending(flightId, userId, provider, price);
        bookingRepository.save(booking);
        outboxPort.save(OutboxEvent.unpublished(
                booking.id(),
                "BOOKING_CREATED",
                toJson(Map.of("eventType", "BOOKING_CREATED",
                              "bookingId", booking.id().toString(),
                              "flightId", flightId,
                              "provider", provider))
        ));
        return booking;
    }

    private String toJson(Map<String, String> data) {
        try {
            return mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize outbox payload", e);
        }
    }
}
