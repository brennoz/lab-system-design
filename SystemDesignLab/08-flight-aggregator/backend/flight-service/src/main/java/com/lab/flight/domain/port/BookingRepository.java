package com.lab.flight.domain.port;

import com.lab.flight.domain.model.Booking;
import com.lab.flight.domain.model.BookingStatus;

import java.util.Optional;
import java.util.UUID;

// Port — persists booking aggregate; updateStatus used by saga handlers
public interface BookingRepository {
    void save(Booking booking);
    Optional<Booking> findById(UUID id);
    void updateStatus(UUID id, BookingStatus status);
}
