package com.lab.webcrawler.domain;

import com.lab.webcrawler.domain.service.LinkExtractor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LinkExtractorTest {

    private final LinkExtractor extractor = new LinkExtractor();

    @Test
    void extracts_absolute_links_from_html() {
        String html = "<html><body>" +
                "<a href='https://bbc.com/sport'>Sport</a>" +
                "<a href='https://bbc.com/news'>News</a>" +
                "</body></html>";

        List<String> links = extractor.extract(html, "https://bbc.com");

        assertThat(links).containsExactlyInAnyOrder("https://bbc.com/sport", "https://bbc.com/news");
    }

    @Test
    void resolves_relative_links_against_base_url() {
        // Why: most real pages use relative links like "/news/article-1", not absolute URLs
        String html = "<html><body><a href='/news/article-1'>Article</a></body></html>";

        List<String> links = extractor.extract(html, "https://bbc.com");

        assertThat(links).containsExactly("https://bbc.com/news/article-1");
    }

    @Test
    void ignores_non_http_links() {
        String html = "<html><body>" +
                "<a href='mailto:test@bbc.com'>Email</a>" +
                "<a href='https://bbc.com/news'>News</a>" +
                "</body></html>";

        List<String> links = extractor.extract(html, "https://bbc.com");

        assertThat(links).containsExactly("https://bbc.com/news");
    }
}
