package com.lab.flight.application.saga;

import com.lab.flight.domain.model.BookingStatus;
import com.lab.flight.domain.port.BookingRepository;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.UUID;

// Pattern: Choreography Saga step — PAYMENT_CONFIRMED → mark COMPLETED → BOOKING_COMPLETED
public class CompleteBookingSaga {

    private final BookingRepository bookingRepository;
    private final KafkaTemplate<String, String> kafka;

    public CompleteBookingSaga(BookingRepository bookingRepository,
                               KafkaTemplate<String, String> kafka) {
        this.bookingRepository = bookingRepository;
        this.kafka = kafka;
    }

    public void handle(String bookingId) {
        UUID id = UUID.fromString(bookingId);
        bookingRepository.updateStatus(id, BookingStatus.COMPLETED);
        kafka.send(HoldFlightSaga.TOPIC,
                "{\"eventType\":\"BOOKING_COMPLETED\",\"bookingId\":\"" + id + "\"}");
    }
}
