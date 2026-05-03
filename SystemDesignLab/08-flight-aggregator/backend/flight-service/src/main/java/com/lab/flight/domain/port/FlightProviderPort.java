package com.lab.flight.domain.port;

import com.lab.flight.domain.model.Flight;
import com.lab.flight.domain.model.SearchQuery;

import java.util.List;

// Port — one implementation per airline provider; providerId() used by saga to route hold calls
public interface FlightProviderPort {
    String providerId();
    List<Flight> search(SearchQuery query);
    String hold(String flightId);
}
