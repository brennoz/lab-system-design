package com.lab.urlshortener.application.usecase;

import com.lab.urlshortener.domain.model.ShortCode;
import com.lab.urlshortener.domain.model.Url;
import com.lab.urlshortener.domain.port.BloomFilterPort;
import com.lab.urlshortener.domain.port.UrlRepository;
import com.lab.urlshortener.domain.service.Base62Encoder;

// Pattern: CQRS write side — one class, one action: persist URL and return its short code
// No Spring annotations — wired by AppConfig so domain/application layers stay framework-free
public class ShortenUrlUseCase {

    private final UrlRepository urlRepository;
    private final BloomFilterPort bloomFilter;
    private final Base62Encoder encoder;

    public ShortenUrlUseCase(UrlRepository urlRepository,
                             BloomFilterPort bloomFilter,
                             Base62Encoder encoder) {
        this.urlRepository = urlRepository;
        this.bloomFilter = bloomFilter;
        this.encoder = encoder;
    }

    public ShortCode shorten(String originalUrl) {
        Url saved = urlRepository.save(originalUrl);
        String code = encoder.encode(saved.id());
        bloomFilter.add(code);
        return new ShortCode(code);
    }
}
