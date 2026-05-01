package com.lab.webcrawler.domain;

import com.lab.webcrawler.domain.service.UrlNormaliser;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UrlNormaliserTest {

    private final UrlNormaliser normaliser = new UrlNormaliser();

    @Test
    void strips_fragment() {
        // Why: "bbc.com/news#comments" and "bbc.com/news" are the same page
        Optional<String> result = normaliser.normalise("https://bbc.com/news#comments");
        assertThat(result).hasValue("https://bbc.com/news");
    }

    @Test
    void strips_utm_tracking_params() {
        // Why: tracking params create infinite URL variations of the same page — crawler trap
        Optional<String> result = normaliser.normalise("https://bbc.com/news?utm_source=twitter&utm_medium=social");
        assertThat(result).hasValue("https://bbc.com/news");
    }

    @Test
    void lowercases_scheme_and_host() {
        Optional<String> result = normaliser.normalise("HTTPS://BBC.COM/News");
        assertThat(result).hasValue("https://bbc.com/News");
    }

    @Test
    void rejects_non_http_scheme() {
        // Why: mailto:, ftp:, javascript: are not crawlable web pages
        Optional<String> result = normaliser.normalise("mailto:test@example.com");
        assertThat(result).isEmpty();
    }
}
