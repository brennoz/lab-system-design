package com.lab.flight.infrastructure.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lab.flight.domain.model.Flight;
import com.lab.flight.domain.model.SearchQuery;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.web.client.RestTemplate;

import java.util.List;

// Pattern: Circuit Breaker — resilience4j wraps each provider call; OPEN → fail-fast, skip in scatter-gather
public class ProviderBAdapter extends AbstractProviderAdapter {

    public ProviderBAdapter(RestTemplate restTemplate, String baseUrl, ObjectMapper mapper) {
        super(restTemplate, baseUrl, mapper);
    }

    @Override
    public String providerId() { return "PROVIDER_B"; }

    @Override
    @CircuitBreaker(name = "providerB")
    public List<Flight> search(SearchQuery query) { return super.search(query); }

    @Override
    @CircuitBreaker(name = "providerB")
    public String hold(String flightId) { return super.hold(flightId); }
}
