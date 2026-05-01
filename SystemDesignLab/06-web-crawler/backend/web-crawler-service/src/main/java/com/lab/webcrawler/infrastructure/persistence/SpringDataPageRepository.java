package com.lab.webcrawler.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

// Pattern: Spring Data port — generated implementation of findAll + count for free
public interface SpringDataPageRepository extends JpaRepository<PageJpaEntity, Long> {
}
