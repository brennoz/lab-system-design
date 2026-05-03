package com.lab.flight.application;

import com.lab.flight.application.usecase.SearchFlightsUseCase;
import com.lab.flight.domain.model.Flight;
import com.lab.flight.domain.model.SearchQuery;
import com.lab.flight.domain.port.FlightCachePort;
import com.lab.flight.domain.port.FlightProviderPort;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SearchFlightsUseCaseTest {

    private final FlightCachePort cache = mock(FlightCachePort.class);
    private final FlightProviderPort providerA = mock(FlightProviderPort.class);
    private final FlightProviderPort providerB = mock(FlightProviderPort.class);
    private final FlightProviderPort providerC = mock(FlightProviderPort.class);

    private final SearchFlightsUseCase useCase =
            new SearchFlightsUseCase(List.of(providerA, providerB, providerC), cache);

    private final SearchQuery query = new SearchQuery("LHR", "JFK", LocalDate.of(2025, 6, 1));

    private Flight flight(String id, String provider) {
        return new Flight(id, "LHR", "JFK", Instant.now(), BigDecimal.valueOf(450), provider);
    }

    @Test
    void returns_cached_results_without_calling_providers() {
        List<Flight> cached = List.of(flight("F1", "A"), flight("F2", "B"));
        when(cache.get(query)).thenReturn(Optional.of(cached));

        List<Flight> result = useCase.search(query);

        assertThat(result).hasSize(2);
        verifyNoInteractions(providerA, providerB, providerC);
    }

    @Test
    void aggregates_all_providers_on_cache_miss_and_caches_result() {
        when(cache.get(query)).thenReturn(Optional.empty());
        when(providerA.search(query)).thenReturn(List.of(flight("A1", "A"), flight("A2", "A")));
        when(providerB.search(query)).thenReturn(List.of(flight("B1", "B")));
        when(providerC.search(query)).thenReturn(List.of(flight("C1", "C"), flight("C2", "C")));

        List<Flight> result = useCase.search(query);

        assertThat(result).hasSize(5);
        verify(cache).put(eq(query), anyList());
    }

    @Test
    void returns_partial_results_when_one_provider_throws() {
        when(cache.get(query)).thenReturn(Optional.empty());
        when(providerA.search(query)).thenReturn(List.of(flight("A1", "A")));
        when(providerB.search(query)).thenThrow(new RuntimeException("CB open"));
        when(providerC.search(query)).thenReturn(List.of(flight("C1", "C")));

        List<Flight> result = useCase.search(query);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Flight::provider).containsExactlyInAnyOrder("A", "C");
    }
}
