package com.lab.webcrawler.domain.port;

import com.lab.webcrawler.domain.model.Domain;

// Pattern: Port (Politeness) — domain checks/sets domain lock; Redis SET EX adapter enforces 1 req/sec per domain
public interface PolitenessPort {

    // true = domain is rate-limited, skip this URL for now
    boolean isLocked(Domain domain);

    // Lock domain after fetch; adapter sets Redis key with TTL
    void lock(Domain domain);
}
