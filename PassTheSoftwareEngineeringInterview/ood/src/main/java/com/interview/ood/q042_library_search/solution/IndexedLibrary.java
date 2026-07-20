package com.interview.ood.q042_library_search.solution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Beyond the given ticket's scope — lesson 0002 section 4. Every other search in this
// package scans the whole catalogue per call (O(n)). This one builds an inverted index —
// whole word (lowercased) -> the set of books containing it, in title or author — so a
// lookup is O(1) average instead of a full scan. The tradeoff: it only matches whole
// tokens, not arbitrary substrings (see IndexedLibraryTest — "lor" matches nothing even
// though "Lord" contains it), unlike Library.search()'s StringUtils.containsIgnoreCase.
public class IndexedLibrary {

    private final Map<String, Set<Book>> index = new HashMap<>();

    public void add(Book book) {
        indexTokensOf(book.getTitle(), book);
        indexTokensOf(book.getAuthor(), book);
    }

    public List<Book> search(String token) {
        return new ArrayList<>(index.getOrDefault(token.toLowerCase(), Set.of()));
    }

    private void indexTokensOf(String text, Book book) {
        for (String token : text.toLowerCase().split("\\s+")) {
            index.computeIfAbsent(token, key -> new HashSet<>()).add(book);
        }
    }
}
