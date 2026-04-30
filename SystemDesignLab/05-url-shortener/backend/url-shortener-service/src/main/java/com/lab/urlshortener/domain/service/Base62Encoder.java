package com.lab.urlshortener.domain.service;

// Algorithm: Base62 encoding — maps auto-increment DB id to URL-safe short code
// Alphabet: 0-9 a-z A-Z (62 chars, no + or / that would break URLs)
// Complexity: O(log₆₂ n) encode/decode; ~6 chars for up to 56 billion URLs
public class Base62Encoder {

    private static final String ALPHABET =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = 62;

    public String encode(long id) {
        StringBuilder result = new StringBuilder();
        while (id > 0) {
            result.insert(0, ALPHABET.charAt((int) (id % BASE)));
            id /= BASE;
        }
        return result.toString();
    }

    public long decode(String code) {
        long result = 0;
        for (char c : code.toCharArray()) {
            result = result * BASE + ALPHABET.indexOf(c);
        }
        return result;
    }
}
