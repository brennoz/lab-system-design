package com.lab.urlshortener.domain.port;

// Pattern: Port (Bloom Filter) — domain calls mightContain; Redis SETBIT/GETBIT impl is the adapter
// Why: domain must not know how bits are stored; only that the probabilistic check exists
public interface BloomFilterPort {

    void add(String value);

    // Returns false = definitely not present (safe 404). Returns true = maybe present (check cache/DB).
    boolean mightContain(String value);
}
