package com.lab.urlshortener.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;

// Pattern: JPA Entity — infrastructure-only; domain Url record has no @Entity annotations
// Why separation: domain must compile without JPA on classpath; entity is an adapter detail
@Entity
@Table(name = "urls")
public class UrlJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Algorithm: auto-increment BIGSERIAL — DB assigns unique id; Base62Encoder derives short code from it
    private Long id;

    @Column(name = "original_url", nullable = false, length = 2048)
    private String originalUrl;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected UrlJpaEntity() {}

    public UrlJpaEntity(String originalUrl, Instant createdAt) {
        this.originalUrl = originalUrl;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getOriginalUrl() { return originalUrl; }
    public Instant getCreatedAt() { return createdAt; }
}
