package com.interview.algorithms.completable_future.q002_invoice_builder.solution;

// REFERENCE TESTS — open only after writing your own MyAnswerTest.java
//
// Compare your test design to these. Pay attention to:
//   1. How to verify PARALLEL execution (not sequential)
//   2. How to stub three services and verify all three are called
//   3. How to test failure propagation with failedFuture()

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Mockito: @ExtendWith(MockitoExtension.class) wires Mockito lifecycle into JUnit 5.
//          It initialises @Mock fields before each test and validates mock interactions after.
@ExtendWith(MockitoExtension.class)
class SolutionTest {

    // Mockito: @Mock creates a test double that records all calls and returns null/0/false
    //          by default. We control its behaviour with when(...).thenReturn(...).
    // WHY mocks and not real implementations: we want to test ONLY the CF pipeline logic
    //     in Solution.buildInvoice(). Real service calls would introduce network latency,
    //     require live infrastructure, and make tests non-deterministic.
    @Mock SubscriptionService subscriptionService;
    @Mock BillingAddressService billingAddressService;
    @Mock PaymentMethodService paymentMethodService;

    Solution underTest;

    @BeforeEach
    void setUp() {
        underTest = new Solution(subscriptionService, billingAddressService, paymentMethodService);
    }

    @Test
    void buildInvoicePopulatesAllFields() throws Exception {
        // CF: completedFuture() gives an already-resolved CF — callbacks run synchronously.
        //     The test gets back a fully resolved Invoice without any real async work.
        var sub  = new Subscription("sub-1", "ANNUAL", BigDecimal.valueOf(1200));
        var addr = new BillingAddress("acc-1", "1 Billing St", "London", "EC1A 1BB");
        var pm   = new PaymentMethod("acc-1", "CARD", "4242");

        when(subscriptionService.getSubscription("sub-1")).thenReturn(CompletableFuture.completedFuture(sub));
        when(billingAddressService.getBillingAddress("acc-1")).thenReturn(CompletableFuture.completedFuture(addr));
        when(paymentMethodService.getDefaultPaymentMethod("acc-1")).thenReturn(CompletableFuture.completedFuture(pm));

        Invoice invoice = underTest.buildInvoice("sub-1", "acc-1").get();

        assertThat(invoice.subscription()).isEqualTo(sub);
        assertThat(invoice.billingAddress()).isEqualTo(addr);
        assertThat(invoice.paymentMethod()).isEqualTo(pm);
        // Verify the amount() accessor delegates correctly
        assertThat(invoice.amount()).isEqualByComparingTo(BigDecimal.valueOf(1200));
    }

    @Test
    void allThreeServicesCalledWithCorrectArguments() throws Exception {
        var sub  = new Subscription("sub-1", "ANNUAL", BigDecimal.valueOf(1200));
        var addr = new BillingAddress("acc-1", "1 Billing St", "London", "EC1A 1BB");
        var pm   = new PaymentMethod("acc-1", "CARD", "4242");

        when(subscriptionService.getSubscription("sub-1")).thenReturn(CompletableFuture.completedFuture(sub));
        when(billingAddressService.getBillingAddress("acc-1")).thenReturn(CompletableFuture.completedFuture(addr));
        when(paymentMethodService.getDefaultPaymentMethod("acc-1")).thenReturn(CompletableFuture.completedFuture(pm));

        underTest.buildInvoice("sub-1", "acc-1").get();

        // Mockito: verify() confirms each service received the correct argument.
        // Catches bugs where the wrong ID is passed (e.g. subscriptionId used for getBillingAddress).
        verify(subscriptionService).getSubscription("sub-1");
        verify(billingAddressService).getBillingAddress("acc-1");
        verify(paymentMethodService).getDefaultPaymentMethod("acc-1");
    }

    @Test
    void billingAddressFailurePropagatesException() {
        // CF: failedFuture(ex) returns a CF that is already failed.
        //     allOf() will propagate this failure — the returned Invoice CF also fails.
        var sub = new Subscription("sub-1", "ANNUAL", BigDecimal.valueOf(1200));
        when(subscriptionService.getSubscription("sub-1")).thenReturn(CompletableFuture.completedFuture(sub));
        when(billingAddressService.getBillingAddress("acc-1"))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Address service unavailable")));
        when(paymentMethodService.getDefaultPaymentMethod("acc-1"))
            .thenReturn(CompletableFuture.completedFuture(new PaymentMethod("acc-1", "CARD", "4242")));
        // WHY stub all 3 services even when one fails: allOf() fires all CFs before checking
        // results. If paymentMethodService is not stubbed, it returns null and causes a
        // NullPointerException before we even get to the address failure assertion.

        assertThatThrownBy(() -> underTest.buildInvoice("sub-1", "acc-1").get())
            .isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class)
            .hasMessageContaining("Address service unavailable");
    }

    @Test
    void allThreeFetchesFireBeforeAnyResultIsUsed() throws Exception {
        // This is the most important test — it verifies PARALLEL execution.
        // Technique: use unresolved CFs (new CompletableFuture<>()) and an AtomicInteger.
        // All three service calls must increment the counter BEFORE any CF resolves.
        // If the implementation is sequential (chain one after another), only 1 call
        // will have been made by the time we check callCount.
        AtomicInteger callCount = new AtomicInteger(0);

        CompletableFuture<Subscription>   subCf  = new CompletableFuture<>();
        CompletableFuture<BillingAddress> addrCf = new CompletableFuture<>();
        CompletableFuture<PaymentMethod>  pmCf   = new CompletableFuture<>();

        // thenAnswer fires the lambda when the mock method is called — increments counter
        // and returns the unresolved CF. The CF stays unresolved until we call .complete() below.
        when(subscriptionService.getSubscription(any())).thenAnswer(inv -> { callCount.incrementAndGet(); return subCf; });
        when(billingAddressService.getBillingAddress(any())).thenAnswer(inv -> { callCount.incrementAndGet(); return addrCf; });
        when(paymentMethodService.getDefaultPaymentMethod(any())).thenAnswer(inv -> { callCount.incrementAndGet(); return pmCf; });

        // buildInvoice() must NOT block — it fires all three service calls and returns a CF
        CompletableFuture<Invoice> result = underTest.buildInvoice("sub-1", "acc-1");

        // All three service methods must have been called synchronously during buildInvoice().
        // A sequential implementation would have called only 1 here (the first in the chain).
        assertThat(callCount.get())
            .as("All 3 services must fire before any CF resolves")
            .isEqualTo(3);

        // Now resolve all CFs — allows the invoice to be assembled
        subCf.complete(new Subscription("sub-1", "ANNUAL", BigDecimal.valueOf(1200)));
        addrCf.complete(new BillingAddress("acc-1", "1 Billing St", "London", "EC1A 1BB"));
        pmCf.complete(new PaymentMethod("acc-1", "CARD", "4242"));
        assertThat(result.get()).isNotNull();
    }
}
