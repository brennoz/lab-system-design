package com.interview.ood.q042_library_search.solution;

import org.apache.commons.lang3.StringUtils;

public class TitleContainsSpecification implements BookSpecification {

    private final String fragment;

    public TitleContainsSpecification(String fragment) {
        this.fragment = fragment;
    }

    @Override
    public boolean isSatisfiedBy(Book book) {
        // Why StringUtils.containsIgnoreCase, not manual toLowerCase().contains():
        // commons-lang3 was on the classpath and unused in the given code — a hint
        // toward the intended tool, not decoration.
        return StringUtils.containsIgnoreCase(book.getTitle(), fragment);
    }
}
