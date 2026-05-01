package com.lab.webcrawler.application.usecase;

import com.lab.webcrawler.domain.model.CrawlUrl;
import com.lab.webcrawler.domain.port.SeenFilterPort;
import com.lab.webcrawler.domain.port.UrlQueuePort;

import java.util.List;

// Pattern: CQRS write side — accepts seed URLs from API, deduplicates via Bloom Filter, enqueues for crawling
// Seeds start at depth=0; child links incremented in CrawlUrlUseCase
public class SubmitSeedUseCase {

    private final SeenFilterPort seenFilter;
    private final UrlQueuePort urlQueue;

    public SubmitSeedUseCase(SeenFilterPort seenFilter, UrlQueuePort urlQueue) {
        this.seenFilter = seenFilter;
        this.urlQueue = urlQueue;
    }

    public void submit(List<String> urls) {
        for (String url : urls) {
            if (!seenFilter.mightContain(url)) {
                seenFilter.add(url);
                urlQueue.enqueue(new CrawlUrl(url, 0));
            }
        }
    }
}
