package com.lab.flight.domain.port;

import com.lab.flight.domain.model.Flight;
import com.lab.flight.domain.model.SearchQuery;

import java.util.List;
import java.util.Optional;

// Port — Cache-Aside; Redis adapter sets TTL 30s on put
public interface FlightCachePort {
    Optional<List<Flight>> get(SearchQuery query);
    void put(SearchQuery query, List<Flight> flights);
}
