package com.lab.webcrawler.infrastructure.persistence;

import com.lab.webcrawler.domain.model.CrawlPage;
import com.lab.webcrawler.domain.port.PageRepository;

// Pattern: Repository adapter — translates domain CrawlPage to JPA entity
public class JpaPageRepository implements PageRepository {

    private final SpringDataPageRepository delegate;

    public JpaPageRepository(SpringDataPageRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public void save(CrawlPage page) {
        delegate.save(new PageJpaEntity(page.url(), page.htmlContent(), page.extractedLinks(), page.crawledAt()));
    }

    @Override
    public long countCrawled() {
        return delegate.count();
    }
}
