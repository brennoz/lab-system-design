package com.lab.webcrawler.domain;

import com.lab.webcrawler.domain.model.Domain;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomainTest {

    @Test
    void extracts_host_from_https_url() {
        Domain domain = Domain.from("https://bbc.com/news/article-1");
        assertThat(domain.value()).isEqualTo("bbc.com");
    }

    @Test
    void lowercases_domain() {
        // Why: "BBC.COM" and "bbc.com" are the same host — must normalise before politeness lock key
        Domain domain = Domain.from("https://BBC.COM/news");
        assertThat(domain.value()).isEqualTo("bbc.com");
    }

    @Test
    void rejects_invalid_url() {
        assertThatThrownBy(() -> Domain.from("not-a-url"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
