package com.lab.flight.api;

import com.lab.flight.api.dto.BookingRequest;
import com.lab.flight.api.dto.BookingResponse;
import com.lab.flight.application.usecase.CreateBookingUseCase;
import com.lab.flight.domain.model.Booking;
import com.lab.flight.domain.port.BookingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final CreateBookingUseCase createUseCase;
    private final BookingRepository bookingRepository;

    public BookingController(CreateBookingUseCase createUseCase,
                              BookingRepository bookingRepository) {
        this.createUseCase = createUseCase;
        this.bookingRepository = bookingRepository;
    }

    // 202 Accepted — Saga runs async; poll GET /bookings/{id} for final status
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public BookingResponse create(@RequestBody BookingRequest req) {
        Booking booking = createUseCase.create(req.flightId(), req.userId(),
                req.provider(), req.price());
        return toResponse(booking);
    }

    @GetMapping("/{id}")
    public BookingResponse get(@PathVariable UUID id) {
        return bookingRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private BookingResponse toResponse(Booking b) {
        return new BookingResponse(b.id(), b.flightId(), b.userId(), b.provider(),
                b.price(), b.status(), b.createdAt());
    }
}
