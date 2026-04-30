package com.lab.urlshortener.domain.port;

import java.util.Optional;

// Pattern: Port (Cache-Aside) — domain describes what caching does; Redis impl is plugged in at runtime
public interface CachePort {

    Optional<String> get(String key);

    void put(String key, String value);
}
