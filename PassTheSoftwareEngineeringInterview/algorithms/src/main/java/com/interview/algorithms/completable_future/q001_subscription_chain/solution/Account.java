package com.interview.algorithms.completable_future.q001_subscription_chain.solution;

// Account lives in a separate bounded context from Subscription — different microservice,
// different DB schema. Fetching account and subscription separately avoids coupling
// between services. This is the standard Spring Boot microservices pattern.
public record Account(String id, String name, String email) {}
