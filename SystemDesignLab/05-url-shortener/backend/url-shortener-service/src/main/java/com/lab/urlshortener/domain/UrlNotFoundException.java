package com.lab.urlshortener.domain;

// Domain error — thrown when Bloom Filter or DB confirms code does not exist
public class UrlNotFoundException extends RuntimeException {

    public UrlNotFoundException(String code) {
        super("Short URL not found: " + code);
    }
}
