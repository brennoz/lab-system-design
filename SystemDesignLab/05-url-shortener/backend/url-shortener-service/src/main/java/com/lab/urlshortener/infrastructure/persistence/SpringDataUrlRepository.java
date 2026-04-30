package com.lab.urlshortener.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

// Spring Data magic: interface only — Spring generates INSERT/SELECT implementation at runtime
public interface SpringDataUrlRepository extends JpaRepository<UrlJpaEntity, Long> {
}
