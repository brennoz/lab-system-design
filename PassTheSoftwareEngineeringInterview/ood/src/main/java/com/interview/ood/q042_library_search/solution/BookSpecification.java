package com.interview.ood.q042_library_search.solution;

// Pattern: Specification — a business rule (a search criterion) as an object with a
// single isSatisfiedBy(candidate) method, composable via combinators like OrSpecification
public interface BookSpecification {
    boolean isSatisfiedBy(Book book);
}
