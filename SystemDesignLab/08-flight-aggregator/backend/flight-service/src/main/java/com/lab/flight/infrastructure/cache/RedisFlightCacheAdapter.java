package com.lab.flight.infrastructure.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lab.flight.domain.model.Flight;
import com.lab.flight.domain.model.SearchQuery;
import com.lab.flight.domain.port.FlightCachePort;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

// Pattern: Cache-Aside — app controls Redis; TTL 30s reflects volatile flight prices
public class RedisFlightCacheAdapter implements FlightCachePort {

    private static final Duration TTL = Duration.ofSeconds(30);

    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;

    public RedisFlightCacheAdapter(StringRedisTemplate redis, ObjectMapper mapper) {
        this.redis = redis;
        this.mapper = mapper;
    }

    @Override
    public Optional<List<Flight>> get(SearchQuery query) {
        String json = redis.opsForValue().get(query.cacheKey());
        if (json == null) return Optional.empty();
        try {
            return Optional.of(mapper.readValue(json, new TypeReference<>() {}));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void put(SearchQuery query, List<Flight> flights) {
        try {
            redis.opsForValue().set(query.cacheKey(), mapper.writeValueAsString(flights), TTL);
        } catch (Exception ignored) {}
    }
}
