package com.lab.webcrawler.infrastructure.persistence;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.List;

// Pattern: JPA Entity — maps CrawlPage aggregate to crawled_pages table
@Entity
@Table(name = "crawled_pages")
public class PageJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2048)
    private String url;

    @Column(columnDefinition = "TEXT")
    private String htmlContent;

    @ElementCollection
    @CollectionTable(name = "crawled_page_links", joinColumns = @JoinColumn(name = "page_id"))
    @Column(name = "link", length = 2048)
    private List<String> extractedLinks;

    @Column(nullable = false)
    private Instant crawledAt;

    protected PageJpaEntity() {}

    public PageJpaEntity(String url, String htmlContent, List<String> extractedLinks, Instant crawledAt) {
        this.url = url;
        this.htmlContent = htmlContent;
        this.extractedLinks = extractedLinks;
        this.crawledAt = crawledAt;
    }

    public String getUrl() { return url; }
    public String getHtmlContent() { return htmlContent; }
    public List<String> getExtractedLinks() { return extractedLinks; }
    public Instant getCrawledAt() { return crawledAt; }
}
