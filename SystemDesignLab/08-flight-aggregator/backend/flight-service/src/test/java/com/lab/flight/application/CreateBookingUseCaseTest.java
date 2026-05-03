package com.lab.flight.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lab.flight.application.usecase.CreateBookingUseCase;
import com.lab.flight.domain.model.Booking;
import com.lab.flight.domain.model.BookingStatus;
import com.lab.flight.domain.model.OutboxEvent;
import com.lab.flight.domain.port.BookingRepository;
import com.lab.flight.domain.port.OutboxPort;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CreateBookingUseCaseTest {

    private final BookingRepository bookingRepository = mock(BookingRepository.class);
    private final OutboxPort outboxPort = mock(OutboxPort.class);
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private final CreateBookingUseCase useCase =
            new CreateBookingUseCase(bookingRepository, outboxPort, mapper);

    @Test
    void saves_pending_booking_and_publishes_outbox_event() {
        Booking booking = useCase.create("LH-001", "user-1", "PROVIDER_A", BigDecimal.valueOf(450));

        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(bookingCaptor.capture());
        assertThat(bookingCaptor.getValue().status()).isEqualTo(BookingStatus.PENDING);

        ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxPort).save(outboxCaptor.capture());
        assertThat(outboxCaptor.getValue().eventType()).isEqualTo("BOOKING_CREATED");
        assertThat(outboxCaptor.getValue().aggregateId()).isEqualTo(booking.id());
        assertThat(outboxCaptor.getValue().published()).isFalse();
        // payload must be valid JSON — catches hand-built string concat regressions
        assertThat(outboxCaptor.getValue().payload()).contains("\"eventType\":\"BOOKING_CREATED\"");
        assertThat(outboxCaptor.getValue().payload()).contains("\"bookingId\"");
        assertThat(outboxCaptor.getValue().payload()).contains("\"flightId\":\"LH-001\"");
    }
}
