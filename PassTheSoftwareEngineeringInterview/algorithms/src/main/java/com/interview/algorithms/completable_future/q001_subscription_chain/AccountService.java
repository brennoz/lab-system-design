package com.interview.algorithms.completable_future.q001_subscription_chain;

import java.util.concurrent.CompletableFuture;

public interface AccountService {
    CompletableFuture<Account> findById(String accountId);
}
