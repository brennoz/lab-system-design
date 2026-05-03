package com.lab.flight.infrastructure.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lab.flight.application.saga.BookingFailedHandler;
import com.lab.flight.application.saga.CompleteBookingSaga;
import com.lab.flight.application.saga.ConfirmPaymentSaga;
import com.lab.flight.application.saga.HoldFlightSaga;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

// Pattern: Choreography router — single listener routes by eventType to the correct saga handler
@Component
public class KafkaSagaConsumer {

    private final HoldFlightSaga holdSaga;
    private final ConfirmPaymentSaga confirmSaga;
    private final CompleteBookingSaga completeSaga;
    private final BookingFailedHandler failedHandler;
    private final ObjectMapper mapper;

    public KafkaSagaConsumer(HoldFlightSaga holdSaga, ConfirmPaymentSaga confirmSaga,
                             CompleteBookingSaga completeSaga, BookingFailedHandler failedHandler,
                             ObjectMapper mapper) {
        this.holdSaga = holdSaga;
        this.confirmSaga = confirmSaga;
        this.completeSaga = completeSaga;
        this.failedHandler = failedHandler;
        this.mapper = mapper;
    }

    @KafkaListener(topics = "flight.booking.events", groupId = "flight-service")
    public void onEvent(String message) {
        try {
            JsonNode node = mapper.readTree(message);
            String eventType = node.get("eventType").asText();
            String bookingId = node.get("bookingId").asText();
            switch (eventType) {
                case "BOOKING_CREATED"    -> holdSaga.handle(bookingId);
                case "FLIGHT_HELD"        -> confirmSaga.handle(bookingId);
                case "PAYMENT_CONFIRMED"  -> completeSaga.handle(bookingId);
                case "BOOKING_FAILED"     -> failedHandler.handle(bookingId);
                default -> { /* ignore unknown events */ }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to process saga event: " + message, e);
        }
    }
}
