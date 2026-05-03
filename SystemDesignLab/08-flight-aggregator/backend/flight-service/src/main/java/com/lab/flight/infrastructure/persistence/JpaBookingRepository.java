package com.lab.flight.infrastructure.persistence;

import com.lab.flight.domain.model.Booking;
import com.lab.flight.domain.model.BookingStatus;
import com.lab.flight.domain.port.BookingRepository;

import java.util.Optional;
import java.util.UUID;

// Pattern: Repository adapter — maps between Booking record and BookingJpaEntity
public class JpaBookingRepository implements BookingRepository {

    private final SpringDataBookingRepository delegate;

    public JpaBookingRepository(SpringDataBookingRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public void save(Booking booking) {
        delegate.save(new BookingJpaEntity(booking.id(), booking.flightId(), booking.userId(),
                booking.provider(), booking.price(), booking.status(), booking.createdAt()));
    }

    @Override
    public Optional<Booking> findById(UUID id) {
        return delegate.findById(id).map(e ->
                new Booking(e.getId(), e.getFlightId(), e.getUserId(), e.getProvider(),
                        e.getPrice(), e.getStatus(), e.getCreatedAt()));
    }

    @Override
    public void updateStatus(UUID id, BookingStatus status) {
        delegate.findById(id).ifPresent(e -> {
            e.setStatus(status);
            delegate.save(e);
        });
    }
}
