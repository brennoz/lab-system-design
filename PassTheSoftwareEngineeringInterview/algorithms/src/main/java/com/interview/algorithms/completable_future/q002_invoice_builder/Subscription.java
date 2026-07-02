package com.interview.algorithms.completable_future.q002_invoice_builder;

import java.math.BigDecimal;

public record Subscription(String id, String planName, BigDecimal amount) {}
