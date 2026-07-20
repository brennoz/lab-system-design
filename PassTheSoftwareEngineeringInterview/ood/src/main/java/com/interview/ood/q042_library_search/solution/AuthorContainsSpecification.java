package com.interview.ood.q042_library_search.solution;

import org.apache.commons.lang3.StringUtils;

public class AuthorContainsSpecification implements BookSpecification {

    private final String fragment;

    public AuthorContainsSpecification(String fragment) {
        this.fragment = fragment;
    }

    @Override
    public boolean isSatisfiedBy(Book book) {
        return StringUtils.containsIgnoreCase(book.getAuthor(), fragment);
    }
}
