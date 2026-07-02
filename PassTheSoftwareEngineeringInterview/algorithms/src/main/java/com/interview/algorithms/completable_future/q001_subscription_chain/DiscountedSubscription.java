package com.interview.algorithms.completable_future.q001_subscription_chain;

import java.math.BigDecimal;

public record DiscountedSubscription(String id, String accountId, String planName, BigDecimal discountedAmount) {}
