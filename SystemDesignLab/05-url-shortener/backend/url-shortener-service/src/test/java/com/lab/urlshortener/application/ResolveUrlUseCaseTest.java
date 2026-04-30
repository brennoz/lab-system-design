package com.lab.urlshortener.application;

import com.lab.urlshortener.application.usecase.ResolveUrlUseCase;
import com.lab.urlshortener.domain.UrlNotFoundException;
import com.lab.urlshortener.domain.model.Url;
import com.lab.urlshortener.domain.port.BloomFilterPort;
import com.lab.urlshortener.domain.port.CachePort;
import com.lab.urlshortener.domain.port.UrlRepository;
import com.lab.urlshortener.domain.service.Base62Encoder;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ResolveUrlUseCaseTest {

    private final UrlRepository urlRepository = mock(UrlRepository.class);
    private final CachePort cache = mock(CachePort.class);
    private final BloomFilterPort bloomFilter = mock(BloomFilterPort.class);
    private final Base62Encoder encoder = new Base62Encoder();
    private final ResolveUrlUseCase useCase =
            new ResolveUrlUseCase(urlRepository, cache, bloomFilter, encoder);

    @Test
    void resolve_returns_url_from_cache_on_hit() {
        String code = encoder.encode(1L);
        when(bloomFilter.mightContain(code)).thenReturn(true);
        when(cache.get("url:" + code)).thenReturn(Optional.of("https://google.com"));

        String result = useCase.resolve(code);

        assertThat(result).isEqualTo("https://google.com");
        verifyNoInteractions(urlRepository);
    }

    @Test
    void resolve_queries_db_on_cache_miss_and_populates_cache() {
        String code = encoder.encode(1L);
        Url url = new Url(1L, "https://google.com", Instant.now());
        when(bloomFilter.mightContain(code)).thenReturn(true);
        when(cache.get("url:" + code)).thenReturn(Optional.empty());
        when(urlRepository.findById(1L)).thenReturn(Optional.of(url));

        String result = useCase.resolve(code);

        assertThat(result).isEqualTo("https://google.com");
        verify(cache).put("url:" + code, "https://google.com");
    }

    @Test
    void resolve_throws_not_found_when_bloom_filter_definite_miss() {
        String code = "xyz99";
        when(bloomFilter.mightContain(code)).thenReturn(false);

        assertThatThrownBy(() -> useCase.resolve(code))
                .isInstanceOf(UrlNotFoundException.class);
        verifyNoInteractions(urlRepository);
        verifyNoInteractions(cache);
    }
}
