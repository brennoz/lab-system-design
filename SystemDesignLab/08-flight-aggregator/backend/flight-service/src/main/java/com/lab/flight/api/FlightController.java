package com.lab.flight.api;

import com.lab.flight.application.usecase.SearchFlightsUseCase;
import com.lab.flight.domain.model.Flight;
import com.lab.flight.domain.model.SearchQuery;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/flights")
public class FlightController {

    private final SearchFlightsUseCase searchUseCase;

    public FlightController(SearchFlightsUseCase searchUseCase) {
        this.searchUseCase = searchUseCase;
    }

    @GetMapping("/search")
    public List<Flight> search(
            @RequestParam String origin,
            @RequestParam String destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return searchUseCase.search(new SearchQuery(origin, destination, date));
    }
}
