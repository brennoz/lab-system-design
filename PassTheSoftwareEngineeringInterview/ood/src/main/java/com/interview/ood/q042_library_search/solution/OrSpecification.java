package com.interview.ood.q042_library_search.solution;

// Combinator — satisfied if either wrapped specification is satisfied. Lets Library.search()
// combine title/author criteria without knowing anything about how either is evaluated.
public class OrSpecification implements BookSpecification {

    private final BookSpecification left;
    private final BookSpecification right;

    public OrSpecification(BookSpecification left, BookSpecification right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean isSatisfiedBy(Book book) {
        return left.isSatisfiedBy(book) || right.isSatisfiedBy(book);
    }
}
