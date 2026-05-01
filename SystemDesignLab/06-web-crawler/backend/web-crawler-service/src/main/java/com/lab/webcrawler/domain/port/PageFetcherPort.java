package com.lab.webcrawler.domain.port;

// Pattern: Port (HTTP Fetcher) — domain requests a page by URL; RestTemplate adapter does actual HTTP
// Why interface: in tests, swap with mock that returns static HTML without network calls
public interface PageFetcherPort {

    // Returns raw HTML content of the page, or empty string on fetch failure
    String fetch(String url);
}
