package com.lab.flight.application.usecase;

import com.lab.flight.domain.model.Flight;
import com.lab.flight.domain.model.SearchQuery;
import com.lab.flight.domain.port.FlightCachePort;
import com.lab.flight.domain.port.FlightProviderPort;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

// Pattern: Scatter-Gather — fans out to N providers in parallel; 1.5s deadline; partial results on timeout/failure
public class SearchFlightsUseCase {

    private static final long DEADLINE_MS = 1_500;

    private final List<FlightProviderPort> providers;
    private final FlightCachePort cache;

    public SearchFlightsUseCase(List<FlightProviderPort> providers, FlightCachePort cache) {
        this.providers = providers;
        this.cache = cache;
    }

    public List<Flight> search(SearchQuery query) {
        Optional<List<Flight>> cached = cache.get(query);
        if (cached.isPresent()) {
            return cached.get();
        }

        // Each future swallows its own exception — CB-open or timeout returns empty list
        List<CompletableFuture<List<Flight>>> futures = providers.stream()
                .map(p -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return p.search(query);
                    } catch (Exception e) {
                        return List.<Flight>of();
                    }
                }))
                .toList();

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(DEADLINE_MS, TimeUnit.MILLISECONDS);
        } catch (Exception ignored) {
            // collect whatever completed within deadline
        }

        List<Flight> results = futures.stream()
                .filter(f -> f.isDone() && !f.isCompletedExceptionally())
                .flatMap(f -> {
                    try { return f.get().stream(); }
                    catch (Exception e) { return java.util.stream.Stream.of(); }
                })
                .toList();

        if (!results.isEmpty()) {
            cache.put(query, results);
        }
        return results;
    }
}
