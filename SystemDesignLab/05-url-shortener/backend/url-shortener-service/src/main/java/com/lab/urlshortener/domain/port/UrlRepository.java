package com.lab.urlshortener.domain.port;

import com.lab.urlshortener.domain.model.Url;

import java.util.Optional;

// Pattern: Repository port — domain defines the contract; infrastructure provides the impl
// Why interface here: domain must not import JPA, Spring, or any persistence technology
public interface UrlRepository {

    // Persists originalUrl, returns Url with DB-assigned id (used by Base62Encoder to derive code)
    Url save(String originalUrl);

    Optional<Url> findById(long id);
}
