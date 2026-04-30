package com.lab.urlshortener.infrastructure.persistence;

import com.lab.urlshortener.domain.model.Url;
import com.lab.urlshortener.domain.port.UrlRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

// Pattern: Adapter — implements domain port using JPA; translates between JPA entity and domain record
// Spring @Repository allowed here: infrastructure layer is where Spring annotations belong
@Repository
public class JpaUrlRepository implements UrlRepository {

    private final SpringDataUrlRepository springDataRepo;

    public JpaUrlRepository(SpringDataUrlRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public Url save(String originalUrl) {
        UrlJpaEntity entity = new UrlJpaEntity(originalUrl, Instant.now());
        UrlJpaEntity saved = springDataRepo.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Url> findById(long id) {
        return springDataRepo.findById(id).map(this::toDomain);
    }

    private Url toDomain(UrlJpaEntity entity) {
        return new Url(entity.getId(), entity.getOriginalUrl(), entity.getCreatedAt());
    }
}
