package com.interview.algorithms.completable_future.q001_subscription_chain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MyAnswerTest {

    @Mock SubscriptionService subscriptionService;
    @Mock PricingService pricingService;
    @Mock AccountService accountService;

    MyAnswer underTest;

    @BeforeEach
    void setUp() {
        underTest = new MyAnswer(subscriptionService, pricingService, accountService);
    }

    @Test
    void enrichReturnsCombinedSubscriptionAndAccount() throws Exception {
        var sub        = new Subscription("sub-1", "acc-1", "ANNUAL", BigDecimal.valueOf(1200));
        var discounted = new DiscountedSubscription("sub-1", "acc-1", "ANNUAL", BigDecimal.valueOf(1080));
        var account    = new Account("acc-1", "Acme Corp", "billing@acme.com");

        when(subscriptionService.findById("sub-1")).thenReturn(CompletableFuture.completedFuture(sub));
        when(pricingService.applyDiscount(sub)).thenReturn(discounted);
        when(accountService.findById("acc-1")).thenReturn(CompletableFuture.completedFuture(account));

        EnrichedSubscription result = underTest.enrich("sub-1").get();

        assertThat(result.subscription()).isEqualTo(discounted);
        assertThat(result.account()).isEqualTo(account);
    }

    @Test
    void accountFetchedUsingSubscriptionAccountId() throws Exception {
        // accountId "acc-99" comes from the subscription — not hardcoded
        var sub        = new Subscription("sub-1", "acc-99", "MONTHLY", BigDecimal.valueOf(100));
        var discounted = new DiscountedSubscription("sub-1", "acc-99", "MONTHLY", BigDecimal.valueOf(90));
        var account    = new Account("acc-99", "Beta Ltd", "pay@beta.com");

        when(subscriptionService.findById("sub-1")).thenReturn(CompletableFuture.completedFuture(sub));
        when(pricingService.applyDiscount(sub)).thenReturn(discounted);
        when(accountService.findById("acc-99")).thenReturn(CompletableFuture.completedFuture(account));

        underTest.enrich("sub-1").get();

        verify(accountService).findById("acc-99");
        // If your implementation hardcodes an accountId, accountService.findById("acc-1") would be called instead → test fails
    }

    @Test
    void discountAppliedAndReflectedInResult() throws Exception {
        var sub        = new Subscription("sub-1", "acc-1", "ANNUAL", BigDecimal.valueOf(1200));
        var discounted = new DiscountedSubscription("sub-1", "acc-1", "ANNUAL", BigDecimal.valueOf(1080));
        var account    = new Account("acc-1", "Acme Corp", "billing@acme.com");

        when(subscriptionService.findById("sub-1")).thenReturn(CompletableFuture.completedFuture(sub));
        when(pricingService.applyDiscount(sub)).thenReturn(discounted);
        when(accountService.findById("acc-1")).thenReturn(CompletableFuture.completedFuture(account));

        EnrichedSubscription result = underTest.enrich("sub-1").get();

        // discountedAmount 1080, not original 1200
        assertThat(result.subscription().discountedAmount()).isEqualByComparingTo(BigDecimal.valueOf(1080));
    }

    @Test
    void subscriptionNotFoundCompletesExceptionally() {
        when(subscriptionService.findById("sub-999"))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Subscription not found: sub-999")));

        assertThatThrownBy(() -> underTest.enrich("sub-999").get())
            .isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class)
            .hasMessageContaining("Subscription not found");
    }
}
