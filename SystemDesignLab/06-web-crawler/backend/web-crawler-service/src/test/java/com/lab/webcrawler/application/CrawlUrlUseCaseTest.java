package com.lab.webcrawler.application;

import com.lab.webcrawler.application.usecase.CrawlUrlUseCase;
import com.lab.webcrawler.domain.model.CrawlUrl;
import com.lab.webcrawler.domain.model.Domain;
import com.lab.webcrawler.domain.port.*;
import com.lab.webcrawler.domain.service.LinkExtractor;
import com.lab.webcrawler.domain.service.UrlNormaliser;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CrawlUrlUseCaseTest {

    private final PageFetcherPort fetcher = mock(PageFetcherPort.class);
    private final SeenFilterPort seenFilter = mock(SeenFilterPort.class);
    private final UrlQueuePort urlQueue = mock(UrlQueuePort.class);
    private final PolitenessPort politeness = mock(PolitenessPort.class);
    private final PageRepository pageRepository = mock(PageRepository.class);
    private final LinkExtractor linkExtractor = new LinkExtractor();
    private final UrlNormaliser normaliser = new UrlNormaliser();

    private final CrawlUrlUseCase useCase = new CrawlUrlUseCase(
            fetcher, seenFilter, urlQueue, politeness, pageRepository, linkExtractor, normaliser);

    @Test
    void crawls_page_extracts_links_and_saves() {
        CrawlUrl crawlUrl = new CrawlUrl("https://bbc.com", 0);
        String html = "<html><body><a href='https://bbc.com/news'>News</a></body></html>";

        when(politeness.isLocked(Domain.from("https://bbc.com"))).thenReturn(false);
        when(fetcher.fetch("https://bbc.com")).thenReturn(html);
        when(seenFilter.mightContain("https://bbc.com/news")).thenReturn(false);

        useCase.crawl(crawlUrl);

        verify(pageRepository).save(any());
        verify(seenFilter).add("https://bbc.com/news");
        verify(urlQueue).enqueue(new CrawlUrl("https://bbc.com/news", 1));
    }

    @Test
    void re_enqueues_url_when_domain_locked() {
        CrawlUrl crawlUrl = new CrawlUrl("https://bbc.com", 0);
        when(politeness.isLocked(Domain.from("https://bbc.com"))).thenReturn(true);

        useCase.crawl(crawlUrl);

        verify(urlQueue).enqueue(crawlUrl);
        verifyNoInteractions(fetcher, pageRepository);
    }

    @Test
    void skips_child_links_beyond_max_depth() {
        CrawlUrl crawlUrl = new CrawlUrl("https://bbc.com", 3);
        String html = "<html><body><a href='https://bbc.com/news'>News</a></body></html>";

        when(politeness.isLocked(any())).thenReturn(false);
        when(fetcher.fetch("https://bbc.com")).thenReturn(html);

        useCase.crawl(crawlUrl);

        verify(urlQueue, never()).enqueue(any());
        verify(pageRepository).save(any());
    }
}
