package com.lab.flight.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataBookingRepository extends JpaRepository<BookingJpaEntity, UUID> {}
