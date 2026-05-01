package com.lab.webcrawler.domain.port;

// Pattern: Port (Bloom Filter) — domain calls mightContain; Redis SETBIT/GETBIT adapter plugged at runtime
// Add BEFORE enqueuing — prevents same URL entering the queue multiple times
public interface SeenFilterPort {

    void add(String url);

    // false = definitely not seen (safe to enqueue). true = maybe seen (skip).
    boolean mightContain(String url);
}
