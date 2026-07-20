package com.interview.ood.q042_library_search.solution;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

// Beyond the given ticket's scope — lesson 0002 section 3. Library/OrSpecification answer
// the ticket (binary match/no-match); this class answers a different question ("which
// result is most relevant?") by scoring instead of just filtering, then sorting descending.
public class RankedLibrary {

    private final List<Book> books = new ArrayList<>();

    public void add(Book book) {
        books.add(book);
    }

    public List<Book> searchRanked(String query) {
        return books.stream()
                .map(b -> new ScoredBook(b, score(b, query)))
                .filter(scored -> scored.score() > 0)
                .sorted(Comparator.comparingInt(ScoredBook::score).reversed())
                .map(ScoredBook::book)
                .collect(Collectors.toList());
    }

    // Why this tier order: an exact title match is almost certainly what the user meant;
    // a title starting with the query is the next-strongest signal (autocomplete-style);
    // a mid-title substring is weaker; an author-only match is weakest but still relevant
    // enough to include, matching the OR-based contract the plain search() honors.
    private int score(Book book, String query) {
        if (book.getTitle().equalsIgnoreCase(query)) {
            return 100;
        }
        if (StringUtils.startsWithIgnoreCase(book.getTitle(), query)) {
            return 50;
        }
        if (StringUtils.containsIgnoreCase(book.getTitle(), query)) {
            return 10;
        }
        if (StringUtils.containsIgnoreCase(book.getAuthor(), query)) {
            return 5;
        }
        return 0;
    }

    private record ScoredBook(Book book, int score) {
    }
}
