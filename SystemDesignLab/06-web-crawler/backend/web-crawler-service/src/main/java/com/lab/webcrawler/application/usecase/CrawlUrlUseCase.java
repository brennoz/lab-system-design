package com.lab.webcrawler.application.usecase;

import com.lab.webcrawler.domain.model.CrawlPage;
import com.lab.webcrawler.domain.model.CrawlUrl;
import com.lab.webcrawler.domain.model.Domain;
import com.lab.webcrawler.domain.port.*;
import com.lab.webcrawler.domain.service.LinkExtractor;
import com.lab.webcrawler.domain.service.UrlNormaliser;

import java.time.Instant;
import java.util.List;

// Pattern: Command — single crawl step in BFS frontier; politeness-gated, deduplication-gated
public class CrawlUrlUseCase {

    static final int MAX_DEPTH = 3;

    private final PageFetcherPort fetcher;
    private final SeenFilterPort seenFilter;
    private final UrlQueuePort urlQueue;
    private final PolitenessPort politeness;
    private final PageRepository pageRepository;
    private final LinkExtractor linkExtractor;
    private final UrlNormaliser normaliser;

    public CrawlUrlUseCase(PageFetcherPort fetcher, SeenFilterPort seenFilter,
                           UrlQueuePort urlQueue, PolitenessPort politeness,
                           PageRepository pageRepository, LinkExtractor linkExtractor,
                           UrlNormaliser normaliser) {
        this.fetcher = fetcher;
        this.seenFilter = seenFilter;
        this.urlQueue = urlQueue;
        this.politeness = politeness;
        this.pageRepository = pageRepository;
        this.linkExtractor = linkExtractor;
        this.normaliser = normaliser;
    }

    public void crawl(CrawlUrl crawlUrl) {
        Domain domain = Domain.from(crawlUrl.url());
        if (politeness.isLocked(domain)) {
            urlQueue.enqueue(crawlUrl);
            return;
        }
        politeness.lock(domain);

        String html = fetcher.fetch(crawlUrl.url());
        List<String> rawLinks = linkExtractor.extract(html, crawlUrl.url());

        if (crawlUrl.depth() < MAX_DEPTH) {
            for (String rawLink : rawLinks) {
                normaliser.normalise(rawLink).ifPresent(normalisedUrl -> {
                    if (!seenFilter.mightContain(normalisedUrl)) {
                        seenFilter.add(normalisedUrl);
                        urlQueue.enqueue(new CrawlUrl(normalisedUrl, crawlUrl.depth() + 1));
                    }
                });
            }
        }

        pageRepository.save(new CrawlPage(crawlUrl.url(), html, rawLinks, Instant.now()));
    }
}
