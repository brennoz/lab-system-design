package com.lab.urlshortener.infrastructure.config;

import com.lab.urlshortener.application.usecase.ResolveUrlUseCase;
import com.lab.urlshortener.application.usecase.ShortenUrlUseCase;
import com.lab.urlshortener.domain.port.BloomFilterPort;
import com.lab.urlshortener.domain.port.CachePort;
import com.lab.urlshortener.domain.port.UrlRepository;
import com.lab.urlshortener.domain.service.Base62Encoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Pattern: Hexagonal wiring — Spring plugs infrastructure adapters into domain use cases here
// Why @Configuration not @Service on use cases: keeps application layer free of Spring annotations
@Configuration
public class AppConfig {

    @Bean
    public Base62Encoder base62Encoder() {
        return new Base62Encoder();
    }

    @Bean
    public ShortenUrlUseCase shortenUrlUseCase(UrlRepository urlRepository,
                                               BloomFilterPort bloomFilter,
                                               Base62Encoder encoder) {
        return new ShortenUrlUseCase(urlRepository, bloomFilter, encoder);
    }

    @Bean
    public ResolveUrlUseCase resolveUrlUseCase(UrlRepository urlRepository,
                                               CachePort cache,
                                               BloomFilterPort bloomFilter,
                                               Base62Encoder encoder) {
        return new ResolveUrlUseCase(urlRepository, cache, bloomFilter, encoder);
    }
}
