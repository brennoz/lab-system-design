package com.lab.urlshortener.application.usecase;

import com.lab.urlshortener.domain.UrlNotFoundException;
import com.lab.urlshortener.domain.model.Url;
import com.lab.urlshortener.domain.port.BloomFilterPort;
import com.lab.urlshortener.domain.port.CachePort;
import com.lab.urlshortener.domain.port.UrlRepository;
import com.lab.urlshortener.domain.service.Base62Encoder;

// Pattern: CQRS read side — Bloom Filter gates DB access; Cache-Aside avoids redundant DB hits
// No Spring annotations — wired by AppConfig
public class ResolveUrlUseCase {

    private final UrlRepository urlRepository;
    private final CachePort cache;
    private final BloomFilterPort bloomFilter;
    private final Base62Encoder encoder;

    public ResolveUrlUseCase(UrlRepository urlRepository,
                             CachePort cache,
                             BloomFilterPort bloomFilter,
                             Base62Encoder encoder) {
        this.urlRepository = urlRepository;
        this.cache = cache;
        this.bloomFilter = bloomFilter;
        this.encoder = encoder;
    }

    public String resolve(String code) {
        // Bloom Filter: false = definite miss → skip cache and DB entirely
        if (!bloomFilter.mightContain(code)) {
            throw new UrlNotFoundException(code);
        }

        String cacheKey = "url:" + code;
        return cache.get(cacheKey).orElseGet(() -> {
            long id = encoder.decode(code);
            Url url = urlRepository.findById(id)
                    .orElseThrow(() -> new UrlNotFoundException(code));
            // Pattern: Cache-Aside — populate cache on first DB hit
            cache.put(cacheKey, url.originalUrl());
            return url.originalUrl();
        });
    }
}
