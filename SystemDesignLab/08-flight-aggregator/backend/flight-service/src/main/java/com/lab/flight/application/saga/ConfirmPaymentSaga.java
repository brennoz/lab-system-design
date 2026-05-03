package com.lab.flight.application.saga;

import com.lab.flight.domain.model.BookingStatus;
import com.lab.flight.domain.port.BookingRepository;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.UUID;

// Pattern: Choreography Saga step — FLIGHT_HELD → no-op payment → PAYMENT_CONFIRMED
// Why no-op: payment is not the learning goal; Saga state machine and Kafka choreography are
public class ConfirmPaymentSaga {

    private final BookingRepository bookingRepository;
    private final KafkaTemplate<String, String> kafka;

    public ConfirmPaymentSaga(BookingRepository bookingRepository,
                              KafkaTemplate<String, String> kafka) {
        this.bookingRepository = bookingRepository;
        this.kafka = kafka;
    }

    public void handle(String bookingId) {
        UUID id = UUID.fromString(bookingId);
        bookingRepository.updateStatus(id, BookingStatus.CONFIRMED);
        kafka.send(HoldFlightSaga.TOPIC,
                "{\"eventType\":\"PAYMENT_CONFIRMED\",\"bookingId\":\"" + id + "\"}");
    }
}
