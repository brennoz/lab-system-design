package com.lab.flight.infrastructure.provider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lab.flight.domain.model.Flight;
import com.lab.flight.domain.model.SearchQuery;
import com.lab.flight.domain.port.FlightProviderPort;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

// Pattern: Template method — shared HTTP logic; subclasses supply providerId() only
public abstract class AbstractProviderAdapter implements FlightProviderPort {

    protected final RestTemplate restTemplate;
    protected final String baseUrl;
    protected final ObjectMapper mapper;

    protected AbstractProviderAdapter(RestTemplate restTemplate, String baseUrl, ObjectMapper mapper) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.mapper = mapper;
    }

    @Override
    public List<Flight> search(SearchQuery query) {
        // UriComponentsBuilder percent-encodes params — safe for airport codes with special chars
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .pathSegment("search", providerId())
                .queryParam("origin", query.origin())
                .queryParam("destination", query.destination())
                .queryParam("date", query.date().toString())
                .build().toUriString();
        String json = restTemplate.getForObject(url, String.class);
        try {
            return mapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public String hold(String flightId) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .pathSegment("hold", providerId(), flightId)
                .build().toUriString();
        return restTemplate.postForObject(url, null, String.class);
    }
}
