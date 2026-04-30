package com.lab.urlshortener.domain;

import com.lab.urlshortener.domain.model.Url;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class UrlTest {

    @Test
    void stores_id_and_original_url() {
        Instant now = Instant.now();
        Url url = new Url(42L, "https://google.com", now);

        assertThat(url.id()).isEqualTo(42L);
        assertThat(url.originalUrl()).isEqualTo("https://google.com");
        assertThat(url.createdAt()).isEqualTo(now);
    }

    @Test
    void two_urls_with_same_id_are_equal() {
        Instant now = Instant.now();
        Url a = new Url(1L, "https://google.com", now);
        Url b = new Url(1L, "https://google.com", now);

        assertThat(a).isEqualTo(b);
    }
}
