package com.interview.ood.q042_library_search.solution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RankedLibraryTest {

    private static final Book EXACT_TITLE   = new Book("Lord of the Flies", "Fred Perry");
    private static final Book STARTS_WITH   = new Book("Lord of the Flies and Friends", "Anna Lee");
    private static final Book CONTAINS_ONLY = new Book("The Book of Lord of the Flies Analysis", "Jane Doe");
    private static final Book AUTHOR_MATCH  = new Book("War and Peace", "Lord of the Flies Fanclub");
    private static final Book NO_MATCH      = new Book("Harry Potter", "J.K. Rowling");

    private final RankedLibrary library = new RankedLibrary();

    @BeforeEach
    void initialise() {
        library.add(NO_MATCH);
        library.add(AUTHOR_MATCH);
        library.add(CONTAINS_ONLY);
        library.add(STARTS_WITH);
        library.add(EXACT_TITLE);
    }

    @Test
    void searchRankedOrdersExactTitleBeforeStartsWithBeforeContainsBeforeAuthorMatch() {
        var results = library.searchRanked("Lord of the Flies");

        assertEquals(4, results.size(), "matching books");
        assertEquals(EXACT_TITLE, results.get(0));
        assertEquals(STARTS_WITH, results.get(1));
        assertEquals(CONTAINS_ONLY, results.get(2));
        assertEquals(AUTHOR_MATCH, results.get(3));
    }

    @Test
    void searchRankedExcludesBooksWithNoMatch() {
        var results = library.searchRanked("Lord of the Flies");

        assertFalse(results.contains(NO_MATCH));
    }
}
