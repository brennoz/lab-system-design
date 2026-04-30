package com.lab.urlshortener.application;

import com.lab.urlshortener.application.usecase.ShortenUrlUseCase;
import com.lab.urlshortener.domain.model.ShortCode;
import com.lab.urlshortener.domain.model.Url;
import com.lab.urlshortener.domain.port.BloomFilterPort;
import com.lab.urlshortener.domain.port.UrlRepository;
import com.lab.urlshortener.domain.service.Base62Encoder;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ShortenUrlUseCaseTest {

    private final UrlRepository urlRepository = mock(UrlRepository.class);
    private final BloomFilterPort bloomFilter = mock(BloomFilterPort.class);
    private final Base62Encoder encoder = new Base62Encoder();
    private final ShortenUrlUseCase useCase = new ShortenUrlUseCase(urlRepository, bloomFilter, encoder);

    @Test
    void shorten_saves_url_and_returns_encoded_code() {
        Url saved = new Url(125L, "https://google.com", Instant.now());
        when(urlRepository.save("https://google.com")).thenReturn(saved);

        ShortCode code = useCase.shorten("https://google.com");

        assertThat(code.value()).isEqualTo(encoder.encode(125L));
        verify(bloomFilter).add(code.value());
    }

    @Test
    void shorten_adds_code_to_bloom_filter() {
        Url saved = new Url(1L, "https://example.com", Instant.now());
        when(urlRepository.save("https://example.com")).thenReturn(saved);

        ShortCode code = useCase.shorten("https://example.com");

        verify(bloomFilter, times(1)).add(code.value());
    }
}
