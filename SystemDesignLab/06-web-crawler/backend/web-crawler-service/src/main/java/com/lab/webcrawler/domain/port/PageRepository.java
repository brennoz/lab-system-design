package com.lab.webcrawler.domain.port;

import com.lab.webcrawler.domain.model.CrawlPage;

// Pattern: Repository port — domain saves crawled pages; JPA adapter handles PostgreSQL
public interface PageRepository {

    void save(CrawlPage page);

    long countCrawled();
}
