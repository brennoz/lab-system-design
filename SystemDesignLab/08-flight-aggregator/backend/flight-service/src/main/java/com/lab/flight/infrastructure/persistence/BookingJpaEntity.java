package com.lab.flight.infrastructure.persistence;

import com.lab.flight.domain.model.BookingStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

// Pattern: JPA entity — maps bookings table; status stored as STRING for readability
@Entity
@Table(name = "bookings")
public class BookingJpaEntity {

    @Id
    private UUID id;
    @Column(name = "flight_id", nullable = false)
    private String flightId;
    @Column(name = "user_id", nullable = false)
    private String userId;
    @Column(nullable = false)
    private String provider;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected BookingJpaEntity() {}

    public BookingJpaEntity(UUID id, String flightId, String userId, String provider,
                            BigDecimal price, BookingStatus status, Instant createdAt) {
        this.id = id; this.flightId = flightId; this.userId = userId;
        this.provider = provider; this.price = price;
        this.status = status; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getFlightId() { return flightId; }
    public String getUserId() { return userId; }
    public String getProvider() { return provider; }
    public BigDecimal getPrice() { return price; }
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
}
