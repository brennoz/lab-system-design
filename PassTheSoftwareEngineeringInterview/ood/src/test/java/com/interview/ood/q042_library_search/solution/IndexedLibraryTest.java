package com.interview.ood.q042_library_search.solution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IndexedLibraryTest {

    private static final Book LORD_OF_THE_FLIES = new Book("Lord of the Flies", "Fred Perry");
    private static final Book LORD_OF_THE_RINGS = new Book("Lord of the Rings", "John Smith");
    private static final Book LORDSHIP_ESTATES  = new Book("Lordship Estates", "Jane Doe");
    private static final Book HARRY_POTTER      = new Book("Harry Potter", "J.K. Rowling");

    private final IndexedLibrary library = new IndexedLibrary();

    @BeforeEach
    void initialise() {
        library.add(LORD_OF_THE_FLIES);
        library.add(LORD_OF_THE_RINGS);
        library.add(LORDSHIP_ESTATES);
        library.add(HARRY_POTTER);
    }

    @Test
    void indexedSearchFindsWholeWordTokenAcrossTitleAndAuthor() {
        var results = library.search("lord");

        assertEquals(2, results.size(), "matching books");
        assertTrue(results.contains(LORD_OF_THE_FLIES));
        assertTrue(results.contains(LORD_OF_THE_RINGS));
        // "Lordship" is a different token than "lord" once split on whitespace —
        // a whole-word index does not match it, unlike the substring-based Library.search().
        assertFalse(results.contains(LORDSHIP_ESTATES));
        assertFalse(results.contains(HARRY_POTTER));
    }

    @Test
    void indexedSearchDoesNotMatchPartialSubstring() {
        // "lor" is a substring of "Lord" but not a whole token, so the inverted index
        // (built from whitespace-split whole words) returns nothing — this is the
        // tradeoff for O(1) token lookup instead of an O(n) substring scan.
        var results = library.search("lor");

        assertTrue(results.isEmpty());
    }
}
