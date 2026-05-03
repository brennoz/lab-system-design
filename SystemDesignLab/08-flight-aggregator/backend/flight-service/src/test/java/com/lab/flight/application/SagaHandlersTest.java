package com.lab.flight.application;

import com.lab.flight.application.saga.BookingFailedHandler;
import com.lab.flight.application.saga.CompleteBookingSaga;
import com.lab.flight.application.saga.ConfirmPaymentSaga;
import com.lab.flight.application.saga.HoldFlightSaga;
import com.lab.flight.domain.model.Booking;
import com.lab.flight.domain.model.BookingStatus;
import com.lab.flight.domain.port.BookingRepository;
import com.lab.flight.domain.port.FlightProviderPort;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SagaHandlersTest {

    private final BookingRepository bookingRepository = mock(BookingRepository.class);
    private final FlightProviderPort providerA = mock(FlightProviderPort.class);

    @SuppressWarnings("unchecked")
    private final KafkaTemplate<String, String> kafka = mock(KafkaTemplate.class);

    private final HoldFlightSaga holdSaga =
            new HoldFlightSaga(bookingRepository, List.of(providerA), kafka);
    private final ConfirmPaymentSaga confirmSaga =
            new ConfirmPaymentSaga(bookingRepository, kafka);
    private final CompleteBookingSaga completeSaga =
            new CompleteBookingSaga(bookingRepository, kafka);
    private final BookingFailedHandler failedHandler =
            new BookingFailedHandler(bookingRepository);

    private Booking pendingBooking(UUID id) {
        return new Booking(id, "LH-001", "user-1", "PROVIDER_A",
                BigDecimal.valueOf(450), BookingStatus.PENDING, Instant.now());
    }

    @Test
    void hold_saga_updates_booking_to_held_and_publishes_flight_held() {
        UUID id = UUID.randomUUID();
        when(bookingRepository.findById(id)).thenReturn(Optional.of(pendingBooking(id)));
        when(providerA.providerId()).thenReturn("PROVIDER_A");
        when(providerA.hold("LH-001")).thenReturn("HOLD-REF-123");
        // kafka.send().get() — must return a completed future so .get() does not NPE
        when(kafka.send(anyString(), anyString())).thenReturn(CompletableFuture.completedFuture(null));

        holdSaga.handle(id.toString());

        verify(bookingRepository).updateStatus(id, BookingStatus.HELD);
        verify(kafka).send(eq("flight.booking.events"), contains("FLIGHT_HELD"));
    }

    @Test
    void hold_saga_publishes_booking_failed_when_provider_throws() {
        UUID id = UUID.randomUUID();
        when(bookingRepository.findById(id)).thenReturn(Optional.of(pendingBooking(id)));
        when(providerA.providerId()).thenReturn("PROVIDER_A");
        when(providerA.hold("LH-001")).thenThrow(new RuntimeException("provider down"));

        holdSaga.handle(id.toString());

        verify(bookingRepository, never()).updateStatus(id, BookingStatus.HELD);
        verify(kafka).send(eq("flight.booking.events"), contains("BOOKING_FAILED"));
    }

    @Test
    void confirm_payment_saga_updates_to_confirmed_and_publishes_payment_confirmed() {
        UUID id = UUID.randomUUID();
        Booking held = new Booking(id, "LH-001", "user-1", "PROVIDER_A",
                BigDecimal.valueOf(450), BookingStatus.HELD, Instant.now());
        when(bookingRepository.findById(id)).thenReturn(Optional.of(held));

        confirmSaga.handle(id.toString());

        verify(bookingRepository).updateStatus(id, BookingStatus.CONFIRMED);
        verify(kafka).send(eq("flight.booking.events"), contains("PAYMENT_CONFIRMED"));
    }

    @Test
    void complete_saga_updates_to_completed_and_publishes_booking_completed() {
        UUID id = UUID.randomUUID();
        Booking confirmed = new Booking(id, "LH-001", "user-1", "PROVIDER_A",
                BigDecimal.valueOf(450), BookingStatus.CONFIRMED, Instant.now());
        when(bookingRepository.findById(id)).thenReturn(Optional.of(confirmed));

        completeSaga.handle(id.toString());

        verify(bookingRepository).updateStatus(id, BookingStatus.COMPLETED);
        verify(kafka).send(eq("flight.booking.events"), contains("BOOKING_COMPLETED"));
    }

    @Test
    void failed_handler_marks_booking_failed() {
        UUID id = UUID.randomUUID();

        failedHandler.handle(id.toString());

        verify(bookingRepository).updateStatus(id, BookingStatus.FAILED);
    }
}
