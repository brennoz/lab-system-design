package com.interview.algorithms.completable_future.q002_invoice_builder.solution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolutionTest {

    @Mock SubscriptionService subscriptionService;
    @Mock BillingAddressService billingAddressService;
    @Mock PaymentMethodService paymentMethodService;

    Solution underTest;

    @BeforeEach
    void setUp() {
        // 10ms timeout — makes slowServiceTriggersTimeoutFallback fast and deterministic in CI
        underTest = new Solution(subscriptionService, billingAddressService, paymentMethodService, 10L);
    }

    @Test
    void buildInvoicePopulatesAllFields() throws Exception {
        var sub  = new Subscription("sub-1", "ANNUAL", BigDecimal.valueOf(1200));
        var addr = new BillingAddress("acc-1", "1 Billing St", "London", "EC1A 1BB");
        var pm   = new PaymentMethod("acc-1", "CARD", "4242");

        // CF: completedFuture — already-resolved CF. Callbacks fire synchronously on the calling thread.
        //     No thread pool involved in tests — deterministic and fast.
        when(subscriptionService.getSubscription("sub-1")).thenReturn(CompletableFuture.completedFuture(sub));
        when(billingAddressService.getBillingAddress("acc-1")).thenReturn(CompletableFuture.completedFuture(addr));
        when(paymentMethodService.getDefaultPaymentMethod("acc-1")).thenReturn(CompletableFuture.completedFuture(pm));

        Invoice invoice = underTest.buildInvoice("sub-1", "acc-1").get();

        assertThat(invoice.subscription()).isEqualTo(sub);
        assertThat(invoice.billingAddress()).isEqualTo(addr);
        assertThat(invoice.paymentMethod()).isEqualTo(pm);
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

        // Mockito: verify() catches bugs like using subscriptionId where accountId is needed.
        verify(subscriptionService).getSubscription("sub-1");
        verify(billingAddressService).getBillingAddress("acc-1");
        verify(paymentMethodService).getDefaultPaymentMethod("acc-1");
    }

    @Test
    void slowServiceTriggersTimeoutFallback() throws Exception {
        // Technique: return a never-completing CF for billing address.
        // After 10ms, completeOnTimeout() fires and fills the CF with BillingAddress.unknown().
        // The invoice builds normally with degraded data — no exception thrown.
        var sub = new Subscription("sub-1", "ANNUAL", BigDecimal.valueOf(1200));
        var pm  = new PaymentMethod("acc-1", "CARD", "4242");

        when(subscriptionService.getSubscription("sub-1")).thenReturn(CompletableFuture.completedFuture(sub));
        when(billingAddressService.getBillingAddress("acc-1"))
            .thenReturn(new CompletableFuture<>());  // never completes — timeout fires
        when(paymentMethodService.getDefaultPaymentMethod("acc-1")).thenReturn(CompletableFuture.completedFuture(pm));

        // get(2, SECONDS) — well above the 10ms timeout; avoids hanging test on failure
        Invoice invoice = underTest.buildInvoice("sub-1", "acc-1").get(2, TimeUnit.SECONDS);

        // Invoice built normally — completeOnTimeout filled in the fallback
        assertThat(invoice.billingAddress()).isEqualTo(BillingAddress.unknown());
        assertThat(invoice.subscription()).isEqualTo(sub);    // other services unaffected
        assertThat(invoice.paymentMethod()).isEqualTo(pm);
    }

    @Test
    void allThreeFetchesFireBeforeAnyResultIsUsed() throws Exception {
        // Verifies PARALLEL execution — all three service calls must start before any CF resolves.
        // A sequential implementation would only have called 1 service by the time we check.
        AtomicInteger callCount = new AtomicInteger(0);

        CompletableFuture<Subscription>   subCf  = new CompletableFuture<>();
        CompletableFuture<BillingAddress> addrCf = new CompletableFuture<>();
        CompletableFuture<PaymentMethod>  pmCf   = new CompletableFuture<>();

        // thenAnswer increments counter and returns the unresolved CF — doesn't complete it yet
        when(subscriptionService.getSubscription(any()))
            .thenAnswer(inv -> { callCount.incrementAndGet(); return subCf; });
        when(billingAddressService.getBillingAddress(any()))
            .thenAnswer(inv -> { callCount.incrementAndGet(); return addrCf; });
        when(paymentMethodService.getDefaultPaymentMethod(any()))
            .thenAnswer(inv -> { callCount.incrementAndGet(); return pmCf; });

        // buildInvoice() fires all three service calls synchronously before returning the CF
        CompletableFuture<Invoice> result = underTest.buildInvoice("sub-1", "acc-1");

        // If implementation is parallel: all 3 fired before any CF resolved → callCount == 3
        assertThat(callCount.get())
            .as("All 3 services must fire before any CF resolves")
            .isEqualTo(3);

        // Now resolve CFs to allow the invoice to complete
        subCf.complete(new Subscription("sub-1", "ANNUAL", BigDecimal.valueOf(1200)));
        addrCf.complete(new BillingAddress("acc-1", "1 Billing St", "London", "EC1A 1BB"));
        pmCf.complete(new PaymentMethod("acc-1", "CARD", "4242"));
        assertThat(result.get(2, TimeUnit.SECONDS)).isNotNull();
    }
}
