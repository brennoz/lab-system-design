package com.interview.algorithms.completable_future.q002_invoice_builder;

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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MyAnswerTest {

    @Mock SubscriptionService subscriptionService;
    @Mock BillingAddressService billingAddressService;
    @Mock PaymentMethodService paymentMethodService;

    MyAnswer underTest;

    @BeforeEach
    void setUp() {
        underTest = new MyAnswer(subscriptionService, billingAddressService, paymentMethodService);
    }

    @Test
    void buildInvoicePopulatesAllFields() throws Exception {
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

        verify(subscriptionService).getSubscription("sub-1");
        verify(billingAddressService).getBillingAddress("acc-1");
        verify(paymentMethodService).getDefaultPaymentMethod("acc-1");
    }

    @Test
    void billingAddressServiceFailurePropagatesException() {
        var sub = new Subscription("sub-1", "ANNUAL", BigDecimal.valueOf(1200));
        when(subscriptionService.getSubscription("sub-1")).thenReturn(CompletableFuture.completedFuture(sub));
        when(billingAddressService.getBillingAddress("acc-1"))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Address service unavailable")));
        when(paymentMethodService.getDefaultPaymentMethod("acc-1"))
            .thenReturn(CompletableFuture.completedFuture(new PaymentMethod("acc-1", "CARD", "4242")));

        assertThatThrownBy(() -> underTest.buildInvoice("sub-1", "acc-1").get())
            .isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class)
            .hasMessageContaining("Address service unavailable");
    }

    @Test
    void allThreeFetchesStartBeforeAnyResultIsUsed() throws Exception {
        // Tracks how many services were called BEFORE we manually complete any CF
        AtomicInteger callCount = new AtomicInteger(0);

        // Unresolved CFs — won't complete until we call complete() below
        CompletableFuture<Subscription>   subCf  = new CompletableFuture<>();
        CompletableFuture<BillingAddress> addrCf = new CompletableFuture<>();
        CompletableFuture<PaymentMethod>  pmCf   = new CompletableFuture<>();

        when(subscriptionService.getSubscription(any())).thenAnswer(inv -> { callCount.incrementAndGet(); return subCf; });
        when(billingAddressService.getBillingAddress(any())).thenAnswer(inv -> { callCount.incrementAndGet(); return addrCf; });
        when(paymentMethodService.getDefaultPaymentMethod(any())).thenAnswer(inv -> { callCount.incrementAndGet(); return pmCf; });

        // Trigger — does NOT block (buildInvoice must not call join/get internally)
        CompletableFuture<Invoice> result = underTest.buildInvoice("sub-1", "acc-1");

        // All three services must have been called already, before any CF resolved
        assertThat(callCount.get())
            .as("All 3 services should be called before any CF resolves (parallel, not sequential)")
            .isEqualTo(3);

        // Now complete all CFs so the overall future can resolve
        subCf.complete(new Subscription("sub-1", "ANNUAL", BigDecimal.valueOf(1200)));
        addrCf.complete(new BillingAddress("acc-1", "1 Billing St", "London", "EC1A 1BB"));
        pmCf.complete(new PaymentMethod("acc-1", "CARD", "4242"));

        assertThat(result.get()).isNotNull();
    }
}
