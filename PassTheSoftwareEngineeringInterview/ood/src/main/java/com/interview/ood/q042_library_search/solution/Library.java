package com.interview.ood.q042_library_search.solution;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Library {

    private final List<Book> books = new ArrayList<>();

    // Why Specification over an inline lambda: the given ticket already combines two
    // criteria (title OR author) in its first version — a signal more criteria are coming.
    // Each future criterion becomes a new BookSpecification class; this method never
    // changes again. See topics/ood/library-system/0002-search-strategies.html section 2
    // for why this earns its place on this ticket specifically, and section 5 for how it
    // extends into q030_library_system's BookCopy/availability model.
    public List<Book> search(String searchString) {
        BookSpecification spec = new OrSpecification(
                new TitleContainsSpecification(searchString),
                new AuthorContainsSpecification(searchString));
        return books.stream()
                .filter(spec::isSatisfiedBy)
                .collect(Collectors.toList());
    }

    public void add(Book book) {
        books.add(book);
    }
}
