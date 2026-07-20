package com.interview.ood.q042_library_search.solution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SolutionTest {

    private static final Book LORD_OF_THE_FLIES = new Book("Lord of the Flies", "Fred Perry");
    private static final Book LORD_OF_THE_RINGS = new Book("Lord of the Rings", "John Smith");
    private static final Book WAR_AND_PEACE     = new Book("War and peace", "Lord Bennett");
    private static final Book HARRY_POTTER      = new Book("Harry Potter", "J.K. Rowling");

    private final Library library = new Library();

    @BeforeEach
    void initialise() {
        library.add(LORD_OF_THE_FLIES);
        library.add(LORD_OF_THE_RINGS);
        library.add(WAR_AND_PEACE);
    }

    @Test
    void searchReturnsTheSingleBookThatMatchesTheEntireTitle() {
        var results = library.search("Lord of the Flies");
        assertEquals(1, results.size(), "matching books");

        var book = results.get(0);
        assertSame(LORD_OF_THE_FLIES, book);
        assertEquals("Lord of the Flies", book.getTitle());
        assertEquals(0, library.search("no book").size(), "matching books");
    }

    @Test
    void searchReturnsSingleBookThatMatchesCaseInsensitiveTitle() {
        var results = library.search("lord of the flies");
        assertEquals(1, results.size(), "matching books");
        assertEquals("Lord of the Flies", results.get(0).getTitle());
    }

    @Test
    void searchReturnsBookMatchingTitleOrAuthor() {
        var results = library.search("lord");
        assertEquals(3, results.size(), "matching books");
        assertTrue(results.contains(LORD_OF_THE_FLIES));
        assertTrue(results.contains(LORD_OF_THE_RINGS));
        assertTrue(results.contains(WAR_AND_PEACE));
        assertFalse(results.contains(HARRY_POTTER));
    }
}
