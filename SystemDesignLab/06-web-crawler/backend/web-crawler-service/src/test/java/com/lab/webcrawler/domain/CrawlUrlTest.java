package com.lab.webcrawler.domain;

import com.lab.webcrawler.domain.model.CrawlUrl;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrawlUrlTest {

    @Test
    void creates_with_valid_url_and_depth() {
        CrawlUrl crawlUrl = new CrawlUrl("https://bbc.com", 0);
        assertThat(crawlUrl.url()).isEqualTo("https://bbc.com");
        assertThat(crawlUrl.depth()).isEqualTo(0);
    }

    @Test
    void rejects_blank_url() {
        assertThatThrownBy(() -> new CrawlUrl("", 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_negative_depth() {
        assertThatThrownBy(() -> new CrawlUrl("https://bbc.com", -1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
