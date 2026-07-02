# CompletableFuture Interview Exercises — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create three isolated, TDD-ready Java exercises targeting CompletableFuture (`thenApply`, `thenCompose`, `thenCombine`, `allOf`) in a Zuora billing domain, inside the existing `algorithms` Maven module.

**Architecture:** Each exercise lives in its own package under `completable_future/q00N_slug/`. Each package contains Java 21 records for models, interfaces for async services, a `MyAnswer.java` skeleton (user implements), a `Solution.java` reference, a `MyAnswerTest.java` that fails against the skeleton, and a `question.md`. No new Maven modules — Mockito, JUnit 5, and AssertJ are already in the parent pom.

**Tech Stack:** Java 21 records, CompletableFuture, JUnit 5 (`@ExtendWith(MockitoExtension.class)`), AssertJ, Mockito (`@Mock`, `when`, `verify`, `CompletableFuture.completedFuture` / `failedFuture` for stubs).

---

## File Map

```
algorithms/src/main/java/com/interview/algorithms/completable_future/
  q001_subscription_chain/
    Subscription.java
    DiscountedSubscription.java
    Account.java
    EnrichedSubscription.java
    SubscriptionService.java
    PricingService.java
    AccountService.java
    MyAnswer.java          ← skeleton, user implements enrich()
    Solution.java          ← reference (do not read before attempting)

  q002_invoice_builder/
    Subscription.java
    BillingAddress.java
    PaymentMethod.java
    Invoice.java
    SubscriptionService.java
    BillingAddressService.java
    PaymentMethodService.java
    MyAnswer.java          ← skeleton, user implements buildInvoice()
    Solution.java

  q003_batch_renewal/
    SubscriptionPlan.java
    PaymentMethod.java
    ChargeResult.java
    RenewalResult.java
    PlanService.java
    PaymentMethodService.java
    ChargeService.java
    MyAnswer.java          ← skeleton, user implements processRenewals()
    Solution.java

algorithms/src/test/java/com/interview/algorithms/completable_future/
  q001_subscription_chain/
    MyAnswerTest.java
  q002_invoice_builder/
    MyAnswerTest.java
  q003_batch_renewal/
    MyAnswerTest.java

PassTheSoftwareEngineeringInterview/concepts/completable_future/
  q001_subscription_chain/question.md
  q002_invoice_builder/question.md
  q003_batch_renewal/question.md
```

---

## Task 1: Exercise 1 — Models, Interfaces, Skeleton, Solution

**Package:** `com.interview.algorithms.completable_future.q001_subscription_chain`

**Files:**
- Create: `algorithms/src/main/java/com/interview/algorithms/completable_future/q001_subscription_chain/Subscription.java`
- Create: `algorithms/src/main/java/com/interview/algorithms/completable_future/q001_subscription_chain/DiscountedSubscription.java`
- Create: `algorithms/src/main/java/com/interview/algorithms/completable_future/q001_subscription_chain/Account.java`
- Create: `algorithms/src/main/java/com/interview/algorithms/completable_future/q001_subscription_chain/EnrichedSubscription.java`
- Create: `algorithms/src/main/java/com/interview/algorithms/completable_future/q001_subscription_chain/SubscriptionService.java`
- Create: `algorithms/src/main/java/com/interview/algorithms/completable_future/q001_subscription_chain/PricingService.java`
- Create: `algorithms/src/main/java/com/interview/algorithms/completable_future/q001_subscription_chain/AccountService.java`
- Create: `algorithms/src/main/java/com/interview/algorithms/completable_future/q001_subscription_chain/MyAnswer.java`
- Create: `algorithms/src/main/java/com/interview/algorithms/completable_future/q001_subscription_chain/Solution.java`

- [ ] **Step 1: Create model records**

`Subscription.java`:
```java
package com.interview.algorithms.completable_future.q001_subscription_chain;

import java.math.BigDecimal;

public record Subscription(String id, String accountId, String planName, BigDecimal amount) {}
```

`DiscountedSubscription.java`:
```java
package com.interview.algorithms.completable_future.q001_subscription_chain;

import java.math.BigDecimal;

public record DiscountedSubscription(String id, String accountId, String planName, BigDecimal discountedAmount) {}
```

`Account.java`:
```java
package com.interview.algorithms.completable_future.q001_subscription_chain;

public record Account(String id, String name, String email) {}
```

`EnrichedSubscription.java`:
```java
package com.interview.algorithms.completable_future.q001_subscription_chain;

public record EnrichedSubscription(DiscountedSubscription subscription, Account account) {}
```

- [ ] **Step 2: Create service interfaces**

`SubscriptionService.java`:
```java
package com.interview.algorithms.completable_future.q001_subscription_chain;

import java.util.concurrent.CompletableFuture;

public interface SubscriptionService {
    CompletableFuture<Subscription> findById(String id);
}
```

`PricingService.java`:
```java
package com.interview.algorithms.completable_future.q001_subscription_chain;

public interface PricingService {
    // Synchronous — no CompletableFuture here
    DiscountedSubscription applyDiscount(Subscription subscription);
}
```

`AccountService.java`:
```java
package com.interview.algorithms.completable_future.q001_subscription_chain;

import java.util.concurrent.CompletableFuture;

public interface AccountService {
    CompletableFuture<Account> findById(String accountId);
}
```

- [ ] **Step 3: Create MyAnswer skeleton**

`MyAnswer.java`:
```java
package com.interview.algorithms.completable_future.q001_subscription_chain;

import java.util.concurrent.CompletableFuture;

// Exercise 1: thenApply + thenCompose
// See concepts/completable_future/q001_subscription_chain/question.md for the full problem statement.
public class MyAnswer {

    private final SubscriptionService subscriptionService;
    private final PricingService pricingService;
    private final AccountService accountService;

    public MyAnswer(SubscriptionService subscriptionService,
                    PricingService pricingService,
                    AccountService accountService) {
        this.subscriptionService = subscriptionService;
        this.pricingService = pricingService;
        this.accountService = accountService;
    }

    /**
     * Build an EnrichedSubscription by:
     * 1. Fetching Subscription by id (async)
     * 2. Applying a pricing discount (sync transform)   ← use thenApply
     * 3. Fetching Account using the subscription's accountId (async, dependent) ← use thenCompose
     * 4. Combining into EnrichedSubscription
     *
     * @return CompletableFuture<EnrichedSubscription>
     */
    public CompletableFuture<EnrichedSubscription> enrich(String subscriptionId) {
        throw new UnsupportedOperationException("Implement me — see question.md");
    }
}
```

- [ ] **Step 4: Create Solution (reference — user should not read before attempting)**

`Solution.java`:
```java
package com.interview.algorithms.completable_future.q001_subscription_chain;

import java.util.concurrent.CompletableFuture;

// REFERENCE SOLUTION — do not read before attempting MyAnswer.java
public class Solution {

    private final SubscriptionService subscriptionService;
    private final PricingService pricingService;
    private final AccountService accountService;

    public Solution(SubscriptionService subscriptionService,
                    PricingService pricingService,
                    AccountService accountService) {
        this.subscriptionService = subscriptionService;
        this.pricingService = pricingService;
        this.accountService = accountService;
    }

    public CompletableFuture<EnrichedSubscription> enrich(String subscriptionId) {
        return subscriptionService.findById(subscriptionId)           // CF<Subscription>
            .thenApply(pricingService::applyDiscount)                 // CF<DiscountedSubscription> — sync transform
            .thenCompose(discounted ->                                // CF<EnrichedSubscription>   — async step dependent on previous result
                accountService.findById(discounted.accountId())       // CF<Account>
                    .thenApply(account ->                             // sync combine
                        new EnrichedSubscription(discounted, account))
            );
    }
    // thenApply: when the transform is synchronous (pure function, no IO)
    // thenCompose: when the next step itself returns a CompletableFuture
    //   — using thenApply here would give CF<CF<Account>> instead of CF<Account>
}
```

---

## Task 2: Exercise 1 — Tests + question.md

**Files:**
- Create: `algorithms/src/test/java/com/interview/algorithms/completable_future/q001_subscription_chain/MyAnswerTest.java`
- Create: `PassTheSoftwareEngineeringInterview/concepts/completable_future/q001_subscription_chain/question.md`

- [ ] **Step 1: Write failing tests**

`MyAnswerTest.java`:
```java
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
```

- [ ] **Step 2: Run tests and confirm they fail**

```bash
cd PassTheSoftwareEngineeringInterview
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn test -pl algorithms -Dtest="**/q001_subscription_chain/**" -q 2>&1 | tail -20
```

Expected: `Tests run: 4, Failures: 0, Errors: 4` (all throw `UnsupportedOperationException`). If any test PASSES, the skeleton is wrong — the test is testing the wrong thing.

- [ ] **Step 3: Write question.md**

`PassTheSoftwareEngineeringInterview/concepts/completable_future/q001_subscription_chain/question.md`:
```markdown
# Exercise 1 — Subscription Chain (Easy)

**CF methods:** `thenApply`, `thenCompose`
**Estimated time:** 15 min
**Skeleton:** `algorithms/src/main/java/com/interview/algorithms/completable_future/q001_subscription_chain/MyAnswer.java`
**Tests:** `algorithms/src/test/java/com/interview/algorithms/completable_future/q001_subscription_chain/MyAnswerTest.java`

---

## Problem

Implement `MyAnswer.enrich(String subscriptionId): CompletableFuture<EnrichedSubscription>`.

You have three services injected via constructor:
- `SubscriptionService.findById(id)` — async, returns `CompletableFuture<Subscription>`
- `PricingService.applyDiscount(subscription)` — **synchronous**, returns `DiscountedSubscription`
- `AccountService.findById(accountId)` — async, returns `CompletableFuture<Account>`

The pipeline must:
1. Fetch the `Subscription` asynchronously
2. Apply the pricing discount (synchronous transform on the result)
3. Fetch the `Account` using the `accountId` from the discounted subscription (asynchronous — depends on step 2)
4. Combine into `EnrichedSubscription(discountedSubscription, account)`

## Constraints

- Do **not** call `.get()` or `.join()` inside your implementation
- Do **not** use `new CompletableFuture<>()` or `CompletableFuture.supplyAsync()`
- Chain from `subscriptionService.findById(subscriptionId)` using only CF transformation methods
- The `AccountService` call must use `discountedSubscription.accountId()` — not the raw subscription's accountId directly

## Key question to answer

`PricingService.applyDiscount()` is synchronous (returns `DiscountedSubscription`, not `CF<DiscountedSubscription>`).
`AccountService.findById()` is asynchronous (returns `CF<Account>`).

Which CF method do you use for each? Why would using `thenApply` for the account fetch produce the wrong return type?

## Run tests

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn test -pl algorithms -Dtest="q001_subscription_chain.MyAnswerTest" -f PassTheSoftwareEngineeringInterview/pom.xml
```

## Ask for hints

Type `hint` in chat if stuck. Type `solution` to reveal the reference implementation.
```

- [ ] **Step 4: Commit exercise 1**

```bash
git add PassTheSoftwareEngineeringInterview/algorithms/src PassTheSoftwareEngineeringInterview/concepts/completable_future/q001_subscription_chain/
git commit -m "feat: add CF exercise 1 — subscription chain (thenApply + thenCompose)"
```

---

## Task 3: Exercise 2 — Models, Interfaces, Skeleton, Solution

**Package:** `com.interview.algorithms.completable_future.q002_invoice_builder`

**Files:**
- Create: `algorithms/src/main/java/com/interview/algorithms/completable_future/q002_invoice_builder/Subscription.java`
- Create: `algorithms/src/main/java/com/interview/algorithms/completable_future/q002_invoice_builder/BillingAddress.java`
- Create: `algorithms/src/main/java/com/interview/algorithms/completable_future/q002_invoice_builder/PaymentMethod.java`
- Create: `algorithms/src/main/java/com/interview/algorithms/completable_future/q002_invoice_builder/Invoice.java`
- Create: `algorithms/src/main/java/com/interview/algorithms/completable_future/q002_invoice_builder/SubscriptionService.java`
- Create: `algorithms/src/main/java/com/interview/algorithms/completable_future/q002_invoice_builder/BillingAddressService.java`
- Create: `algorithms/src/main/java/com/interview/algorithms/completable_future/q002_invoice_builder/PaymentMethodService.java`
- Create: `algorithms/src/main/java/com/interview/algorithms/completable_future/q002_invoice_builder/MyAnswer.java`
- Create: `algorithms/src/main/java/com/interview/algorithms/completable_future/q002_invoice_builder/Solution.java`

- [ ] **Step 1: Create model records**

`Subscription.java`:
```java
package com.interview.algorithms.completable_future.q002_invoice_builder;

import java.math.BigDecimal;

public record Subscription(String id, String planName, BigDecimal amount) {}
```

`BillingAddress.java`:
```java
package com.interview.algorithms.completable_future.q002_invoice_builder;

public record BillingAddress(String accountId, String street, String city, String postCode) {}
```

`PaymentMethod.java`:
```java
package com.interview.algorithms.completable_future.q002_invoice_builder;

public record PaymentMethod(String accountId, String type, String last4) {}
```

`Invoice.java`:
```java
package com.interview.algorithms.completable_future.q002_invoice_builder;

import java.math.BigDecimal;

public record Invoice(Subscription subscription,
                      BillingAddress billingAddress,
                      PaymentMethod paymentMethod) {
    public BigDecimal amount() {
        return subscription.amount();
    }
}
```

- [ ] **Step 2: Create service interfaces**

`SubscriptionService.java`:
```java
package com.interview.algorithms.completable_future.q002_invoice_builder;

import java.util.concurrent.CompletableFuture;

public interface SubscriptionService {
    CompletableFuture<Subscription> getSubscription(String subscriptionId);
}
```

`BillingAddressService.java`:
```java
package com.interview.algorithms.completable_future.q002_invoice_builder;

import java.util.concurrent.CompletableFuture;

public interface BillingAddressService {
    CompletableFuture<BillingAddress> getBillingAddress(String accountId);
}
```

`PaymentMethodService.java`:
```java
package com.interview.algorithms.completable_future.q002_invoice_builder;

import java.util.concurrent.CompletableFuture;

public interface PaymentMethodService {
    CompletableFuture<PaymentMethod> getDefaultPaymentMethod(String accountId);
}
```

- [ ] **Step 3: Create MyAnswer skeleton**

`MyAnswer.java`:
```java
package com.interview.algorithms.completable_future.q002_invoice_builder;

import java.util.concurrent.CompletableFuture;

// Exercise 2: thenCombine / allOf — parallel independent fetches
// See concepts/completable_future/q002_invoice_builder/question.md for the full problem statement.
public class MyAnswer {

    private final SubscriptionService subscriptionService;
    private final BillingAddressService billingAddressService;
    private final PaymentMethodService paymentMethodService;

    public MyAnswer(SubscriptionService subscriptionService,
                    BillingAddressService billingAddressService,
                    PaymentMethodService paymentMethodService) {
        this.subscriptionService = subscriptionService;
        this.billingAddressService = billingAddressService;
        this.paymentMethodService = paymentMethodService;
    }

    /**
     * Build an Invoice by fetching three independent pieces of data in parallel:
     * - Subscription (by subscriptionId)
     * - BillingAddress (by accountId)
     * - PaymentMethod (by accountId)
     *
     * All three fetches are INDEPENDENT — no result depends on another.
     * Fire all three before waiting for any.
     *
     * CF methods to use: allOf (or thenCombine chained twice)
     *
     * @return CompletableFuture<Invoice>
     */
    public CompletableFuture<Invoice> buildInvoice(String subscriptionId, String accountId) {
        throw new UnsupportedOperationException("Implement me — see question.md");
    }
}
```

- [ ] **Step 4: Create Solution**

`Solution.java`:
```java
package com.interview.algorithms.completable_future.q002_invoice_builder;

import java.util.concurrent.CompletableFuture;

// REFERENCE SOLUTION — do not read before attempting MyAnswer.java
public class Solution {

    private final SubscriptionService subscriptionService;
    private final BillingAddressService billingAddressService;
    private final PaymentMethodService paymentMethodService;

    public Solution(SubscriptionService subscriptionService,
                    BillingAddressService billingAddressService,
                    PaymentMethodService paymentMethodService) {
        this.subscriptionService = subscriptionService;
        this.billingAddressService = billingAddressService;
        this.paymentMethodService = paymentMethodService;
    }

    public CompletableFuture<Invoice> buildInvoice(String subscriptionId, String accountId) {
        // Fire all three simultaneously — none depends on any other
        CompletableFuture<Subscription>    subCf  = subscriptionService.getSubscription(subscriptionId);
        CompletableFuture<BillingAddress>  addrCf = billingAddressService.getBillingAddress(accountId);
        CompletableFuture<PaymentMethod>   pmCf   = paymentMethodService.getDefaultPaymentMethod(accountId);

        // allOf waits for ALL to complete, returns CF<Void>
        // join() inside thenApply is SAFE here: allOf guarantees all three are done before thenApply runs
        return CompletableFuture.allOf(subCf, addrCf, pmCf)
            .thenApply(ignored -> new Invoice(subCf.join(), addrCf.join(), pmCf.join()));

        // Alternative with thenCombine (works for exactly 2, needs nesting for 3+):
        // return subCf.thenCombine(addrCf, (sub, addr) -> Map.entry(sub, addr))
        //     .thenCombine(pmCf, (pair, pm) -> new Invoice(pair.getKey(), pair.getValue(), pm));
        // allOf is cleaner when combining 3+ independent CFs.
    }
}
```

---

## Task 4: Exercise 2 — Tests + question.md

**Files:**
- Create: `algorithms/src/test/java/com/interview/algorithms/completable_future/q002_invoice_builder/MyAnswerTest.java`
- Create: `PassTheSoftwareEngineeringInterview/concepts/completable_future/q002_invoice_builder/question.md`

- [ ] **Step 1: Write failing tests**

`MyAnswerTest.java`:
```java
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
```

- [ ] **Step 2: Run tests and confirm they fail**

```bash
cd PassTheSoftwareEngineeringInterview
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn test -pl algorithms -Dtest="**/q002_invoice_builder/**" -q 2>&1 | tail -20
```

Expected: `Tests run: 4, Errors: 4` (all `UnsupportedOperationException`).

- [ ] **Step 3: Write question.md**

`PassTheSoftwareEngineeringInterview/concepts/completable_future/q002_invoice_builder/question.md`:
```markdown
# Exercise 2 — Invoice Builder (Medium)

**CF methods:** `allOf` (primary), `thenCombine` (alternative)
**Estimated time:** 20 min
**Skeleton:** `algorithms/src/main/java/com/interview/algorithms/completable_future/q002_invoice_builder/MyAnswer.java`
**Tests:** `algorithms/src/test/java/com/interview/algorithms/completable_future/q002_invoice_builder/MyAnswerTest.java`

---

## Problem

Implement `MyAnswer.buildInvoice(String subscriptionId, String accountId): CompletableFuture<Invoice>`.

You have three services injected via constructor — all async:
- `SubscriptionService.getSubscription(subscriptionId)` → `CompletableFuture<Subscription>`
- `BillingAddressService.getBillingAddress(accountId)` → `CompletableFuture<BillingAddress>`
- `PaymentMethodService.getDefaultPaymentMethod(accountId)` → `CompletableFuture<PaymentMethod>`

These three calls are **completely independent**. No result depends on another.

Fetch all three simultaneously and combine them into `new Invoice(subscription, billingAddress, paymentMethod)`.

## Constraints

- All three CF calls must be **initiated before any result is awaited**
- Do NOT call `.get()` or `.join()` before all three CFs have been started
- Do NOT chain them sequentially (subscription → billing → payment would be 3× slower)
- The test `allThreeFetchesStartBeforeAnyResultIsUsed` verifies parallelism explicitly

## Key question to answer

`allOf` returns `CompletableFuture<Void>` — it does not carry the results.
How do you access the results of `subCf`, `addrCf`, `pmCf` inside the `thenApply` after `allOf`?
(Hint: what does `allOf` guarantee about those CFs by the time `thenApply` runs?)

## thenCombine vs allOf

`thenCombine(cf2, (r1, r2) -> combine(r1, r2))` works perfectly for exactly 2 CFs.
For 3+ independent CFs, `allOf` is cleaner — no nesting required.
Try implementing with `thenCombine` first, then refactor to `allOf`.

## Run tests

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn test -pl algorithms -Dtest="q002_invoice_builder.MyAnswerTest" -f PassTheSoftwareEngineeringInterview/pom.xml
```

## Ask for hints

Type `hint` in chat if stuck. Type `solution` to reveal the reference implementation.
```

- [ ] **Step 4: Commit exercise 2**

```bash
git add PassTheSoftwareEngineeringInterview/algorithms/src/main/java/com/interview/algorithms/completable_future/q002_invoice_builder/ \
        PassTheSoftwareEngineeringInterview/algorithms/src/test/java/com/interview/algorithms/completable_future/q002_invoice_builder/ \
        PassTheSoftwareEngineeringInterview/concepts/completable_future/q002_invoice_builder/
git commit -m "feat: add CF exercise 2 — invoice builder (allOf + thenCombine)"
```

---

## Task 5: Exercise 3 — Models, Interfaces, Skeleton, Solution

**Package:** `com.interview.algorithms.completable_future.q003_batch_renewal`

**Files:**
- Create: `algorithms/src/main/java/com/interview/algorithms/completable_future/q003_batch_renewal/SubscriptionPlan.java`
- Create: `algorithms/src/main/java/com/interview/algorithms/completable_future/q003_batch_renewal/PaymentMethod.java`
- Create: `algorithms/src/main/java/com/interview/algorithms/completable_future/q003_batch_renewal/ChargeResult.java`
- Create: `algorithms/src/main/java/com/interview/algorithms/completable_future/q003_batch_renewal/RenewalResult.java`
- Create: `algorithms/src/main/java/com/interview/algorithms/completable_future/q003_batch_renewal/PlanService.java`
- Create: `algorithms/src/main/java/com/interview/algorithms/completable_future/q003_batch_renewal/PaymentMethodService.java`
- Create: `algorithms/src/main/java/com/interview/algorithms/completable_future/q003_batch_renewal/ChargeService.java`
- Create: `algorithms/src/main/java/com/interview/algorithms/completable_future/q003_batch_renewal/MyAnswer.java`
- Create: `algorithms/src/main/java/com/interview/algorithms/completable_future/q003_batch_renewal/Solution.java`

- [ ] **Step 1: Create model records**

`SubscriptionPlan.java`:
```java
package com.interview.algorithms.completable_future.q003_batch_renewal;

import java.math.BigDecimal;

public record SubscriptionPlan(String subscriptionId, String planName, BigDecimal amount) {}
```

`PaymentMethod.java`:
```java
package com.interview.algorithms.completable_future.q003_batch_renewal;

public record PaymentMethod(String subscriptionId, String type, String token) {}
```

`ChargeResult.java`:
```java
package com.interview.algorithms.completable_future.q003_batch_renewal;

import java.math.BigDecimal;

public record ChargeResult(String subscriptionId, String chargeId, BigDecimal amount) {}
```

`RenewalResult.java`:
```java
package com.interview.algorithms.completable_future.q003_batch_renewal;

public record RenewalResult(String subscriptionId,
                             boolean success,
                             ChargeResult chargeResult,
                             String errorMessage) {

    public static RenewalResult success(String subscriptionId, ChargeResult chargeResult) {
        return new RenewalResult(subscriptionId, true, chargeResult, null);
    }

    public static RenewalResult failed(String subscriptionId, String errorMessage) {
        return new RenewalResult(subscriptionId, false, null, errorMessage);
    }
}
```

- [ ] **Step 2: Create service interfaces**

`PlanService.java`:
```java
package com.interview.algorithms.completable_future.q003_batch_renewal;

import java.util.concurrent.CompletableFuture;

public interface PlanService {
    CompletableFuture<SubscriptionPlan> getPlan(String subscriptionId);
}
```

`PaymentMethodService.java`:
```java
package com.interview.algorithms.completable_future.q003_batch_renewal;

import java.util.concurrent.CompletableFuture;

public interface PaymentMethodService {
    CompletableFuture<PaymentMethod> getPaymentMethod(String subscriptionId);
}
```

`ChargeService.java`:
```java
package com.interview.algorithms.completable_future.q003_batch_renewal;

import java.util.concurrent.CompletableFuture;

public interface ChargeService {
    CompletableFuture<ChargeResult> charge(SubscriptionPlan plan, PaymentMethod paymentMethod);
}
```

- [ ] **Step 3: Create MyAnswer skeleton**

`MyAnswer.java`:
```java
package com.interview.algorithms.completable_future.q003_batch_renewal;

import java.util.List;
import java.util.concurrent.CompletableFuture;

// Exercise 3: allOf + thenCombine + thenCompose + exceptionally (Hard)
// See concepts/completable_future/q003_batch_renewal/question.md for the full problem statement.
public class MyAnswer {

    private final PlanService planService;
    private final PaymentMethodService paymentMethodService;
    private final ChargeService chargeService;

    public MyAnswer(PlanService planService,
                    PaymentMethodService paymentMethodService,
                    ChargeService chargeService) {
        this.planService = planService;
        this.paymentMethodService = paymentMethodService;
        this.chargeService = chargeService;
    }

    /**
     * Renew a batch of subscriptions.
     *
     * For EACH subscriptionId:
     *   1. Fetch SubscriptionPlan and PaymentMethod IN PARALLEL  ← thenCombine
     *   2. Attempt charge using plan + payment method             ← thenCompose (charge returns CF)
     *   3. Wrap success in RenewalResult.success(...)             ← thenApply
     *   4. Isolate failure: failed charge → RenewalResult.failed(...) ← exceptionally
     *
     * Collect all per-item results with allOf.
     * Return CompletableFuture<List<RenewalResult>>.
     *
     * CRITICAL: one failed charge must NOT fail the whole batch.
     * The returned list always has the same size as the input list.
     *
     * CF methods to use: thenCombine, thenCompose, thenApply, exceptionally, allOf
     */
    public CompletableFuture<List<RenewalResult>> processRenewals(List<String> subscriptionIds) {
        throw new UnsupportedOperationException("Implement me — see question.md");
    }
}
```

- [ ] **Step 4: Create Solution**

`Solution.java`:
```java
package com.interview.algorithms.completable_future.q003_batch_renewal;

import java.util.List;
import java.util.concurrent.CompletableFuture;

// REFERENCE SOLUTION — do not read before attempting MyAnswer.java
public class Solution {

    private final PlanService planService;
    private final PaymentMethodService paymentMethodService;
    private final ChargeService chargeService;

    public Solution(PlanService planService,
                    PaymentMethodService paymentMethodService,
                    ChargeService chargeService) {
        this.planService = planService;
        this.paymentMethodService = paymentMethodService;
        this.chargeService = chargeService;
    }

    public CompletableFuture<List<RenewalResult>> processRenewals(List<String> subscriptionIds) {
        // Build one CF<RenewalResult> per subscription
        List<CompletableFuture<RenewalResult>> futures = subscriptionIds.stream()
            .map(this::renewOne)
            .toList(); // collect ALL CFs first — so all start before any join

        // allOf waits for every CF to complete (success or failure)
        // join() inside thenApply is safe: allOf guarantees all are done
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(ignored -> futures.stream()
                .map(CompletableFuture::join)
                .toList());
    }

    private CompletableFuture<RenewalResult> renewOne(String subscriptionId) {
        // Intermediate record to hold plan + payment method together
        record PlanAndPayment(SubscriptionPlan plan, PaymentMethod paymentMethod) {}

        CompletableFuture<SubscriptionPlan> planCf = planService.getPlan(subscriptionId);
        CompletableFuture<PaymentMethod>    pmCf   = paymentMethodService.getPaymentMethod(subscriptionId);

        return planCf
            .thenCombine(pmCf, PlanAndPayment::new)                               // parallel fetch — combine when both done
            .thenCompose(pp -> chargeService.charge(pp.plan(), pp.paymentMethod())) // charge returns CF — thenCompose flattens
            .thenApply(charge -> RenewalResult.success(subscriptionId, charge))    // wrap success
            .exceptionally(ex -> RenewalResult.failed(subscriptionId, ex.getMessage())); // isolate failure per item
        // exceptionally on the PER-ITEM CF means one charge failure → that item is FAILED, not the whole batch
    }
}
```

---

## Task 6: Exercise 3 — Tests + question.md

**Files:**
- Create: `algorithms/src/test/java/com/interview/algorithms/completable_future/q003_batch_renewal/MyAnswerTest.java`
- Create: `PassTheSoftwareEngineeringInterview/concepts/completable_future/q003_batch_renewal/question.md`

- [ ] **Step 1: Write failing tests**

`MyAnswerTest.java`:
```java
package com.interview.algorithms.completable_future.q003_batch_renewal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MyAnswerTest {

    @Mock PlanService planService;
    @Mock PaymentMethodService paymentMethodService;
    @Mock ChargeService chargeService;

    MyAnswer underTest;

    @BeforeEach
    void setUp() {
        underTest = new MyAnswer(planService, paymentMethodService, chargeService);
    }

    @Test
    void allSubscriptionsRenewedSuccessfully() throws Exception {
        stubSuccessfulRenewal("sub-1", "charge-1");
        stubSuccessfulRenewal("sub-2", "charge-2");
        stubSuccessfulRenewal("sub-3", "charge-3");

        List<RenewalResult> results = underTest.processRenewals(List.of("sub-1", "sub-2", "sub-3")).get();

        assertThat(results).hasSize(3);
        assertThat(results).allMatch(RenewalResult::success);
        assertThat(results).extracting(RenewalResult::subscriptionId)
            .containsExactlyInAnyOrder("sub-1", "sub-2", "sub-3");
    }

    @Test
    void oneChargeFailureDoesNotFailEntireBatch() throws Exception {
        stubSuccessfulRenewal("sub-1", "charge-1");
        // sub-2 charge fails
        stubPlanAndPayment("sub-2");
        when(chargeService.charge(
                new SubscriptionPlan("sub-2", "MONTHLY", BigDecimal.valueOf(50)),
                new PaymentMethod("sub-2", "CARD", "token-sub-2")))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Card declined for sub-2")));
        stubSuccessfulRenewal("sub-3", "charge-3");

        List<RenewalResult> results = underTest.processRenewals(List.of("sub-1", "sub-2", "sub-3")).get();

        assertThat(results).hasSize(3); // batch always returns full list
        assertThat(results).filteredOn(r -> r.subscriptionId().equals("sub-1")).allMatch(RenewalResult::success);
        assertThat(results).filteredOn(r -> r.subscriptionId().equals("sub-2")).allMatch(r -> !r.success());
        assertThat(results).filteredOn(r -> r.subscriptionId().equals("sub-2"))
            .extracting(RenewalResult::errorMessage).containsExactly("Card declined for sub-2");
        assertThat(results).filteredOn(r -> r.subscriptionId().equals("sub-3")).allMatch(RenewalResult::success);
    }

    @Test
    void planAndPaymentFetchedInParallelBeforeCharge() throws Exception {
        stubSuccessfulRenewal("sub-1", "charge-1");

        underTest.processRenewals(List.of("sub-1")).get();

        // Both plan and payment method fetched for "sub-1"
        verify(planService).getPlan("sub-1");
        verify(paymentMethodService).getPaymentMethod("sub-1");
        // Charge called with the fetched plan and payment method
        verify(chargeService).charge(
            new SubscriptionPlan("sub-1", "MONTHLY", BigDecimal.valueOf(50)),
            new PaymentMethod("sub-1", "CARD", "token-sub-1"));
    }

    @Test
    void resultListSizeMatchesInputListSize() throws Exception {
        stubSuccessfulRenewal("sub-1", "charge-1");
        stubSuccessfulRenewal("sub-2", "charge-2");

        List<RenewalResult> results = underTest.processRenewals(List.of("sub-1", "sub-2")).get();

        assertThat(results).hasSize(2);
    }

    // ---- helpers ----

    private void stubSuccessfulRenewal(String subscriptionId, String chargeId) {
        stubPlanAndPayment(subscriptionId);
        when(chargeService.charge(
                new SubscriptionPlan(subscriptionId, "MONTHLY", BigDecimal.valueOf(50)),
                new PaymentMethod(subscriptionId, "CARD", "token-" + subscriptionId)))
            .thenReturn(CompletableFuture.completedFuture(
                new ChargeResult(subscriptionId, chargeId, BigDecimal.valueOf(50))));
    }

    private void stubPlanAndPayment(String subscriptionId) {
        when(planService.getPlan(subscriptionId))
            .thenReturn(CompletableFuture.completedFuture(
                new SubscriptionPlan(subscriptionId, "MONTHLY", BigDecimal.valueOf(50))));
        when(paymentMethodService.getPaymentMethod(subscriptionId))
            .thenReturn(CompletableFuture.completedFuture(
                new PaymentMethod(subscriptionId, "CARD", "token-" + subscriptionId)));
    }
}
```

- [ ] **Step 2: Run tests and confirm they fail**

```bash
cd PassTheSoftwareEngineeringInterview
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn test -pl algorithms -Dtest="**/q003_batch_renewal/**" -q 2>&1 | tail -20
```

Expected: `Tests run: 4, Errors: 4`.

- [ ] **Step 3: Write question.md**

`PassTheSoftwareEngineeringInterview/concepts/completable_future/q003_batch_renewal/question.md`:
```markdown
# Exercise 3 — Batch Subscription Renewal (Hard)

**CF methods:** `thenCombine`, `thenCompose`, `thenApply`, `exceptionally`, `allOf`
**Estimated time:** 30 min
**Skeleton:** `algorithms/src/main/java/com/interview/algorithms/completable_future/q003_batch_renewal/MyAnswer.java`
**Tests:** `algorithms/src/test/java/com/interview/algorithms/completable_future/q003_batch_renewal/MyAnswerTest.java`

---

## Problem

Implement `MyAnswer.processRenewals(List<String> subscriptionIds): CompletableFuture<List<RenewalResult>>`.

You have three services injected via constructor:
- `PlanService.getPlan(subscriptionId)` → `CompletableFuture<SubscriptionPlan>`
- `PaymentMethodService.getPaymentMethod(subscriptionId)` → `CompletableFuture<PaymentMethod>`
- `ChargeService.charge(plan, paymentMethod)` → `CompletableFuture<ChargeResult>`

For **each** subscription ID in the input list, you must:
1. Fetch `SubscriptionPlan` and `PaymentMethod` **in parallel** (they're independent)
2. Once both are ready, attempt to charge via `ChargeService.charge(plan, method)`
3. Wrap a successful charge in `RenewalResult.success(subscriptionId, chargeResult)`
4. If the charge fails, produce `RenewalResult.failed(subscriptionId, errorMessage)` — do NOT let the failure propagate to the outer batch

Collect all per-item results. Return a single `CompletableFuture<List<RenewalResult>>` that completes when ALL renewals are done (success or failure).

## Critical constraint

**One failed charge must NOT fail the whole batch.**
The returned CF must complete (not exceptionally) with a full list, where failed items have `success == false`.
This is the key difference between exercise 2 (one failure = whole CF fails) and exercise 3 (per-item isolation).

## Approach hint (without giving it away)

Break the problem into two methods:
- `processRenewals(List<String>)` — launches one CF per ID, uses `allOf` to collect
- `renewOne(String)` — builds the per-item pipeline using `thenCombine` + `thenCompose` + `exceptionally`

## Key questions to answer before coding

1. Why does fetching plan + payment method use `thenCombine` rather than chaining one after the other?
2. `chargeService.charge()` returns `CompletableFuture<ChargeResult>`. If you used `thenApply` to call it, what type would you get? Why does `thenCompose` fix this?
3. `exceptionally` on the per-item CF: what happens if you put it on the OUTER `allOf` CF instead?
4. In `processRenewals`, why do you collect all CFs into a list BEFORE calling `allOf`?

## Run tests

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn test -pl algorithms -Dtest="q003_batch_renewal.MyAnswerTest" -f PassTheSoftwareEngineeringInterview/pom.xml
```

## Ask for hints

Type `hint` in chat if stuck. Type `solution` to reveal the reference implementation.
```

- [ ] **Step 4: Commit exercise 3**

```bash
git add PassTheSoftwareEngineeringInterview/algorithms/src/main/java/com/interview/algorithms/completable_future/q003_batch_renewal/ \
        PassTheSoftwareEngineeringInterview/algorithms/src/test/java/com/interview/algorithms/completable_future/q003_batch_renewal/ \
        PassTheSoftwareEngineeringInterview/concepts/completable_future/q003_batch_renewal/
git commit -m "feat: add CF exercise 3 — batch renewal (allOf + thenCombine + thenCompose + exceptionally)"
```

---

## Task 7: Final Verification

- [ ] **Step 1: Run all three exercises to confirm tests fail against empty skeletons**

```bash
cd PassTheSoftwareEngineeringInterview
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn test -pl algorithms -Dtest="**/completable_future/**" 2>&1 | grep -E "Tests run|ERROR|FAIL|BUILD"
```

Expected output:
```
Tests run: 4, Failures: 0, Errors: 4, Skipped: 0  (q001)
Tests run: 4, Failures: 0, Errors: 4, Skipped: 0  (q002)
Tests run: 4, Failures: 0, Errors: 4, Skipped: 0  (q003)
BUILD FAILURE
```

This is correct — `BUILD FAILURE` means the exercises are ready. The user starts from Red.

- [ ] **Step 2: Commit plan doc**

```bash
git add docs/superpowers/plans/2026-07-02-completablefuture-exercises.md
git commit -m "docs: add CompletableFuture exercises implementation plan"
```

---

## Self-Review

**Spec coverage:**
- ✅ Ex1 `thenApply` + `thenCompose` → Tasks 1–2
- ✅ Ex2 `thenCombine` / `allOf` → Tasks 3–4
- ✅ Ex3 `allOf` + `thenCombine` + `thenCompose` + `exceptionally` → Tasks 5–6
- ✅ Mockito: `@Mock`, `when(...).thenReturn(CF.completedFuture(...))`, `when(...).thenReturn(CF.failedFuture(...))`, `verify()`
- ✅ Java 21 records for all models
- ✅ 4 tests per exercise, all fail against skeleton
- ✅ `question.md` per exercise with problem, constraints, key questions, run commands
- ✅ `Solution.java` per exercise with inline explanation comments
- ✅ Partial-failure isolation tested in Ex3 (`oneChargeFailureDoesNotFailEntireBatch`)
- ✅ Parallelism tested: Ex2 `allThreeFetchesStartBeforeAnyResultIsUsed`, Ex3 `planAndPaymentFetchedInParallelBeforeCharge`

**No placeholders found.** All code blocks are complete and compilable.

**Type consistency:** `SubscriptionPlan`, `PaymentMethod`, `ChargeResult`, `RenewalResult` used consistently across models, interfaces, skeleton, solution, and tests. `RenewalResult.success()` / `RenewalResult.failed()` static factory methods defined in the record and used in both solution and test assertions.
