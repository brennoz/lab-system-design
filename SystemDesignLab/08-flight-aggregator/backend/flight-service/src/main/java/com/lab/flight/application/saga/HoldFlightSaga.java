package com.lab.flight.application.saga;

import com.lab.flight.domain.model.Booking;
import com.lab.flight.domain.model.BookingStatus;
import com.lab.flight.domain.port.BookingRepository;
import com.lab.flight.domain.port.FlightProviderPort;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.UUID;

// Pattern: Choreography Saga step — BOOKING_CREATED → hold seat at provider → FLIGHT_HELD | BOOKING_FAILED
public class HoldFlightSaga {

    static final String TOPIC = "flight.booking.events";

    private final BookingRepository bookingRepository;
    private final List<FlightProviderPort> providers;
    private final KafkaTemplate<String, String> kafka;

    public HoldFlightSaga(BookingRepository bookingRepository,
                          List<FlightProviderPort> providers,
                          KafkaTemplate<String, String> kafka) {
        this.bookingRepository = bookingRepository;
        this.providers = providers;
        this.kafka = kafka;
    }

    public void handle(String bookingId) {
        UUID id = UUID.fromString(bookingId);
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Booking not found: " + id));

        try {
            FlightProviderPort provider = providers.stream()
                    .filter(p -> p.providerId().equals(booking.provider()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No provider: " + booking.provider()));
            String holdRef = provider.hold(booking.flightId());
            // Kafka send + ack BEFORE DB update — if send fails, DB stays PENDING and catch sends BOOKING_FAILED
            kafka.send(TOPIC, "{\"eventType\":\"FLIGHT_HELD\",\"bookingId\":\"" + id + "\",\"holdRef\":\"" + holdRef + "\"}").get();
            bookingRepository.updateStatus(id, BookingStatus.HELD);
        } catch (Exception e) {
            kafka.send(TOPIC, "{\"eventType\":\"BOOKING_FAILED\",\"bookingId\":\"" + id + "\"}");
        }
    }
}
