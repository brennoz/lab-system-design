# CF Exercise Restructure Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restructure three existing CompletableFuture exercises so models/interfaces/Solution live in a `solution/` sub-package, MyAnswer is an empty class shell, MyAnswerTest is deleted, and a heavily-commented SolutionTest is added — matching the new exercise structure standard.

**Architecture:** Each exercise's `solution/` sub-package is fully self-contained (own models, interfaces, Solution.java, SolutionTest.java). The main package has only `MyAnswer.java` (empty) and `question.md`. Zero cross-dependency between the two. Both compile independently.

**Tech Stack:** Java 21, Maven, JUnit 5, AssertJ, Mockito. All in `PassTheSoftwareEngineeringInterview/algorithms/` module.

---

## File Map — Before → After

### q001_subscription_chain

```
BEFORE                                          AFTER
src/main/java/.../q001_subscription_chain/      src/main/java/.../q001_subscription_chain/
  Account.java           DELETE                   MyAnswer.java       (stripped to empty shell)
  AccountService.java    DELETE                   question.md         (moved from algorithms/completable_future/)
  DiscountedSubscription.java  DELETE             solution/
  EnrichedSubscription.java    DELETE               Account.java      (new — solution package, commented)
  MyAnswer.java          STRIP                     AccountService.java
  PricingService.java    DELETE                    DiscountedSubscription.java
  Solution.java          MOVE to solution/         EnrichedSubscription.java
  Subscription.java      DELETE                    PricingService.java
  SubscriptionService.java  DELETE                 Solution.java       (moved + commented)
                                                   Subscription.java
                                                   SubscriptionService.java

src/test/java/.../q001_subscription_chain/      src/test/java/.../q001_subscription_chain/
  MyAnswerTest.java      DELETE                   solution/
                                                    SolutionTest.java  (new — heavily commented)

algorithms/completable_future/q001_subscription_chain/
  question.md            MOVE to src/main/java path
```

### q002_invoice_builder (same pattern)
```
BEFORE                                          AFTER
main: BillingAddress, BillingAddressService,    main: MyAnswer.java (empty), question.md
      Invoice, PaymentMethod,                   solution/: all models + interfaces + Solution.java
      PaymentMethodService, Subscription,
      SubscriptionService, Solution.java,
      MyAnswer.java
test: MyAnswerTest.java                         test/solution/: SolutionTest.java
```

### q003_batch_renewal (same pattern)
```
BEFORE                                          AFTER
main: ChargeResult, ChargeService,              main: MyAnswer.java (empty), question.md
      PaymentMethod, PaymentMethodService,      solution/: all models + interfaces + Solution.java
      PlanService, RenewalResult,
      Solution.java, SubscriptionPlan,
      MyAnswer.java
test: MyAnswerTest.java                         test/solution/: SolutionTest.java
```

---

## Task 1: Restructure q001_subscription_chain

**Base path:** `PassTheSoftwareEngineeringInterview/algorithms/src/main/java/com/interview/algorithms/completable_future/q001_subscription_chain/`
**Solution package:** `com.interview.algorithms.completable_future.q001_subscription_chain.solution`

- [ ] **Step 1: Create solution/Subscription.java**

```java
package com.interview.algorithms.completable_future.q001_subscription_chain.solution;

import java.math.BigDecimal;

// Java: Java 21 record — immutable data carrier.
//       Compiler auto-generates constructor, equals(), hashCode(), toString(),
//       and accessor methods (id(), accountId(), planName(), amount()).
// WHY records for domain models: immutability prevents accidental mutation when the same
//       object is passed across multiple async callbacks. Two threads sharing a mutable
//       Subscription could race on its fields — a record eliminates that risk entirely.
// ALT: A plain class with final fields and a constructor works but requires ~30 lines of
//       boilerplate. Records express the same idea in 1 line.
public record Subscription(
    String id,          // unique subscription identifier (e.g. "sub-abc-123")
    String accountId,   // foreign key to the Account that owns this subscription
    String planName,    // billing plan code, e.g. "ANNUAL", "MONTHLY"
    // Java: BigDecimal — NEVER use double or float for monetary values.
    //       double cannot represent 0.10 exactly (IEEE 754 binary fraction).
    //       At scale, rounding errors accumulate into real money discrepancies.
    BigDecimal amount   // full price before any discounts applied
) {}
```

- [ ] **Step 2: Create solution/DiscountedSubscription.java**

```java
package com.interview.algorithms.completable_future.q001_subscription_chain.solution;

import java.math.BigDecimal;

// Java: Separate record for the discounted form of a subscription.
// WHY a distinct type: if we reused Subscription and just mutated the amount field,
//     callers could confuse a discounted amount for an original amount.
//     Distinct types make the compiler enforce correct usage — you cannot pass a
//     DiscountedSubscription where a Subscription is expected without an explicit conversion.
// DDD: This is a Value Object transformation — the same identity (same id, same accountId)
//      but a different representation after the pricing step.
public record DiscountedSubscription(
    String id,
    String accountId,
    String planName,
    BigDecimal discountedAmount  // price after discount; field name differs from Subscription.amount
                                 // to make the distinction explicit at the call site
) {}
```

- [ ] **Step 3: Create solution/Account.java**

```java
package com.interview.algorithms.completable_future.q001_subscription_chain.solution;

// Java: record with three String fields — no BigDecimal needed here,
//       Account is a reference object (billing metadata), not a financial value.
// WHY fetch Account separately from Subscription:
//     In a real Zuora-like billing system, subscriptions and accounts live in
//     different bounded contexts / microservices. Fetching them together (JOIN)
//     would couple the subscription service to the account service.
//     Fetching separately keeps services independent and allows independent scaling.
public record Account(
    String id,    // matches Subscription.accountId — the join key
    String name,  // display name for invoice header
    String email  // billing email for notifications
) {}
```

- [ ] **Step 4: Create solution/EnrichedSubscription.java**

```java
package com.interview.algorithms.completable_future.q001_subscription_chain.solution;

// WHY EnrichedSubscription: this is the output model of the enrichment pipeline.
//     It combines the discounted pricing data with the account metadata needed
//     to generate an invoice or send a billing notification.
// Pattern: Assembler / Enrichment — a common pattern in integration layers
//     where you fetch data from multiple sources and combine into a composite view.
// Java: nested records are perfectly valid. EnrichedSubscription composes two records.
//     Records are value-based (equality by fields), so two EnrichedSubscriptions with
//     identical subscription + account will be equal — useful in tests.
public record EnrichedSubscription(
    DiscountedSubscription subscription, // pricing after discount
    Account account                      // account metadata
) {}
```

- [ ] **Step 5: Create solution/SubscriptionService.java**

```java
package com.interview.algorithms.completable_future.q001_subscription_chain.solution;

import java.util.concurrent.CompletableFuture;

// Java: interface — defines a contract without implementation.
// WHY an interface here:
//     1. Testability — Mockito replaces this with a fake that returns controlled CFs
//     2. Hexagonal architecture — domain code depends on this port; infrastructure
//        (HTTP client, database) is a plug-in adapter behind this interface
//     3. The real implementation (e.g. a REST call to the subscription service)
//        never leaks into the exercise
// CF: method returns CompletableFuture<Subscription> — the call is NON-BLOCKING.
//     The caller gets back a CF immediately and can register callbacks
//     (.thenApply, .thenCompose) without blocking the thread.
public interface SubscriptionService {
    // WHY async: subscription lookup requires a network call or DB query.
    //            Async allows the caller to initiate other work (e.g. fetching the Account)
    //            without waiting for this response first.
    CompletableFuture<Subscription> findById(String id);
}
```

- [ ] **Step 6: Create solution/PricingService.java**

```java
package com.interview.algorithms.completable_future.q001_subscription_chain.solution;

// CF: PricingService is SYNCHRONOUS — it returns DiscountedSubscription directly,
//     NOT CompletableFuture<DiscountedSubscription>.
// WHY: discount calculation is a pure in-memory function — no IO, no network call.
//      Wrapping a sync function in a CF adds overhead with no benefit.
// KEY LEARNING: this is what determines which CF method the caller uses:
//   - sync transform (no IO)  → thenApply(fn)   — fn runs on the same thread as the CF
//   - async transform (has IO) → thenCompose(fn) — fn returns a new CF, flattening CF<CF<T>>
//   Using thenApply on an async function would give CF<CF<Account>> — double-wrapped, wrong type.
public interface PricingService {
    // ALT: could return Optional<DiscountedSubscription> if some plans have no discount.
    //      Kept simple here — always applies a discount transformation.
    DiscountedSubscription applyDiscount(Subscription subscription);
}
```

- [ ] **Step 7: Create solution/AccountService.java**

```java
package com.interview.algorithms.completable_future.q001_subscription_chain.solution;

import java.util.concurrent.CompletableFuture;

// CF: AccountService is ASYNCHRONOUS — returns CompletableFuture<Account>.
//     This is what requires thenCompose in the pipeline (not thenApply).
// WHY: fetching account data requires an external call (another microservice or DB).
//      The account fetch DEPENDS ON the subscriptionId's accountId — it cannot
//      start until the subscription is known. This sequential dependency is expressed
//      with thenCompose: "when the subscription arrives, start an async account fetch".
public interface AccountService {
    CompletableFuture<Account> findById(String accountId);
}
```

- [ ] **Step 8: Create solution/Solution.java**

```java
package com.interview.algorithms.completable_future.q001_subscription_chain.solution;

import java.util.concurrent.CompletableFuture;

// REFERENCE SOLUTION — open only after attempting MyAnswer.java
//
// This class demonstrates the canonical thenApply + thenCompose pipeline.
// Read the inline comments to understand WHY each CF method was chosen.
public class Solution {

    // WHY constructor injection (not field injection):
    //   1. Testable — pass mocks in tests, real adapters in production
    //   2. Immutable — final fields cannot be reassigned after construction
    //   3. Explicit dependencies — caller knows exactly what this class needs
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

    // CF pipeline: findById → thenApply(discount) → thenCompose(accountFetch) → thenApply(combine)
    //
    // Return type is CompletableFuture<EnrichedSubscription> — the method returns IMMEDIATELY
    // with a CF that will eventually hold the result. The caller is never blocked.
    public CompletableFuture<EnrichedSubscription> enrich(String subscriptionId) {

        return subscriptionService.findById(subscriptionId)
            // CF: thenApply — sync transform. PricingService.applyDiscount() is a pure function:
            //     no IO, no network call. thenApply runs the function on the completing thread
            //     and wraps the result in a new CF.
            // WHY NOT thenCompose: applyDiscount returns DiscountedSubscription (not CF<...>).
            //     thenCompose expects a Function<T, CompletableFuture<U>>. Using it for a sync
            //     function would still work, but you'd need to wrap the result in
            //     CompletableFuture.completedFuture(...) — unnecessary overhead.
            .thenApply(pricingService::applyDiscount)          // CF<Subscription> → CF<DiscountedSubscription>

            // CF: thenCompose — async step that DEPENDS on the previous result.
            //     accountService.findById() returns CF<Account> — a new async operation.
            //     thenCompose "flattens" this: instead of CF<CF<Account>>, we get CF<Account>.
            // WHY thenCompose and NOT thenApply:
            //     thenApply(fn) where fn returns CF<Account> would produce CF<CF<Account>>.
            //     That's a nested future — you'd need .get().get() to extract the Account.
            //     thenCompose(fn) automatically unwraps the inner CF, giving CF<Account>.
            .thenCompose(discounted ->                         // CF<DiscountedSubscription> → CF<EnrichedSubscription>
                accountService.findById(discounted.accountId()) // async: CF<Account>
                    // CF: thenApply again — sync combine of discounted + account into output.
                    //     This thenApply is INSIDE the thenCompose lambda, chained on the account CF.
                    .thenApply(account ->
                        new EnrichedSubscription(discounted, account)) // CF<Account> → CF<EnrichedSubscription>
            );
        // ALT: Could use thenCombine if the account fetch were independent of the subscription.
        //      Here it IS dependent (we need discounted.accountId()), so thenCompose is correct.
    }
}
```

- [ ] **Step 9: Strip MyAnswer.java to empty shell**

Replace `algorithms/src/main/java/com/interview/algorithms/completable_future/q001_subscription_chain/MyAnswer.java` with:

```java
package com.interview.algorithms.completable_future.q001_subscription_chain;

/**
 * Exercise 1 — Subscription Chain
 *
 * Read question.md before starting.
 *
 * Design your own models, interfaces, and implement the solution from scratch.
 * Follow TDD: write MyAnswerTest.java first, then make it pass here.
 *
 * Reference: solution/ sub-package contains a fully commented implementation
 * — only open it after you have attempted this exercise.
 */
public class MyAnswer {
    // your implementation here
}
```

- [ ] **Step 10: Delete old main-package files**

```bash
cd /home/brennoz77/LABS/LAB_CLAUDE/PassTheSoftwareEngineeringInterview
git rm algorithms/src/main/java/com/interview/algorithms/completable_future/q001_subscription_chain/Account.java
git rm algorithms/src/main/java/com/interview/algorithms/completable_future/q001_subscription_chain/AccountService.java
git rm algorithms/src/main/java/com/interview/algorithms/completable_future/q001_subscription_chain/DiscountedSubscription.java
git rm algorithms/src/main/java/com/interview/algorithms/completable_future/q001_subscription_chain/EnrichedSubscription.java
git rm algorithms/src/main/java/com/interview/algorithms/completable_future/q001_subscription_chain/PricingService.java
git rm algorithms/src/main/java/com/interview/algorithms/completable_future/q001_subscription_chain/Solution.java
git rm algorithms/src/main/java/com/interview/algorithms/completable_future/q001_subscription_chain/Subscription.java
git rm algorithms/src/main/java/com/interview/algorithms/completable_future/q001_subscription_chain/SubscriptionService.java
```

- [ ] **Step 11: Delete old MyAnswerTest.java**

```bash
git rm algorithms/src/test/java/com/interview/algorithms/completable_future/q001_subscription_chain/MyAnswerTest.java
```

- [ ] **Step 12: Create SolutionTest.java**

Create `algorithms/src/test/java/com/interview/algorithms/completable_future/q001_subscription_chain/solution/SolutionTest.java`:

```java
package com.interview.algorithms.completable_future.q001_subscription_chain.solution;

// REFERENCE TESTS — open only after writing your own MyAnswerTest.java
//
// These tests verify Solution.java. Use them as a guide for how to test
// CompletableFuture pipelines with Mockito. Compare your MyAnswerTest to these.

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Mockito: @ExtendWith(MockitoExtension.class) wires Mockito lifecycle into JUnit 5.
//          It initialises @Mock fields before each test and validates mock interactions after.
@ExtendWith(MockitoExtension.class)
class SolutionTest {

    // Mockito: @Mock creates a test double that records all calls and returns null/0/false
    //          by default. We control its behaviour with when(...).thenReturn(...).
    // WHY mocks and not real implementations: we want to test ONLY the CF pipeline logic
    //     in Solution.enrich(). Real service calls would introduce network latency,
    //     require live infrastructure, and make tests non-deterministic.
    @Mock SubscriptionService subscriptionService;
    @Mock PricingService pricingService;
    @Mock AccountService accountService;

    Solution underTest;

    @BeforeEach
    void setUp() {
        // Constructor injection — mocks passed in, no Spring context needed.
        underTest = new Solution(subscriptionService, pricingService, accountService);
    }

    @Test
    void enrichReturnsCombinedSubscriptionAndAccount() throws Exception {
        // ARRANGE: set up stubs for the entire happy-path pipeline
        var sub        = new Subscription("sub-1", "acc-1", "ANNUAL", BigDecimal.valueOf(1200));
        var discounted = new DiscountedSubscription("sub-1", "acc-1", "ANNUAL", BigDecimal.valueOf(1080));
        var account    = new Account("acc-1", "Acme Corp", "billing@acme.com");

        // Mockito stubs: when(...).thenReturn(...)
        // CF: CompletableFuture.completedFuture(value) returns an ALREADY-COMPLETED CF.
        //     This means thenApply/thenCompose callbacks fire synchronously on the calling thread.
        //     No thread pool involved — tests are deterministic and fast.
        when(subscriptionService.findById("sub-1")).thenReturn(CompletableFuture.completedFuture(sub));
        when(pricingService.applyDiscount(sub)).thenReturn(discounted);
        when(accountService.findById("acc-1")).thenReturn(CompletableFuture.completedFuture(account));

        // ACT: .get() blocks until the CF completes; throws ExecutionException on failure.
        // WHY .get() in tests: we need the result synchronously to assert on it.
        //     In production code, never call .get() — chain with .thenApply instead.
        EnrichedSubscription result = underTest.enrich("sub-1").get();

        // ASSERT: result contains discounted subscription (not original) and correct account
        assertThat(result.subscription()).isEqualTo(discounted);
        assertThat(result.account()).isEqualTo(account);
    }

    @Test
    void accountFetchedUsingDiscountedSubscriptionAccountId() throws Exception {
        // This test verifies that the accountId used for the account fetch comes from
        // the DISCOUNTED subscription, not hardcoded or from the input parameter.
        // We use a different accountId ("acc-99") to make this visible.
        var sub        = new Subscription("sub-1", "acc-99", "MONTHLY", BigDecimal.valueOf(100));
        var discounted = new DiscountedSubscription("sub-1", "acc-99", "MONTHLY", BigDecimal.valueOf(90));
        var account    = new Account("acc-99", "Beta Ltd", "pay@beta.com");

        when(subscriptionService.findById("sub-1")).thenReturn(CompletableFuture.completedFuture(sub));
        when(pricingService.applyDiscount(sub)).thenReturn(discounted);
        when(accountService.findById("acc-99")).thenReturn(CompletableFuture.completedFuture(account));

        underTest.enrich("sub-1").get();

        // Mockito: verify() asserts that the mock was called with specific arguments.
        // WHY: if the implementation hardcodes "acc-1" or reads the raw subscription's
        //      accountId instead of the discounted subscription's accountId, this fails.
        verify(accountService).findById("acc-99");
    }

    @Test
    void discountAmountReflectedInResult() throws Exception {
        var sub        = new Subscription("sub-1", "acc-1", "ANNUAL", BigDecimal.valueOf(1200));
        var discounted = new DiscountedSubscription("sub-1", "acc-1", "ANNUAL", BigDecimal.valueOf(1080));
        var account    = new Account("acc-1", "Acme Corp", "billing@acme.com");

        when(subscriptionService.findById("sub-1")).thenReturn(CompletableFuture.completedFuture(sub));
        when(pricingService.applyDiscount(sub)).thenReturn(discounted);
        when(accountService.findById("acc-1")).thenReturn(CompletableFuture.completedFuture(account));

        EnrichedSubscription result = underTest.enrich("sub-1").get();

        // AssertJ: isEqualByComparingTo for BigDecimal — avoids scale differences.
        //          new BigDecimal("1080") and BigDecimal.valueOf(1080) are equal by value
        //          but may differ by scale; isEqualTo() would fail, isEqualByComparingTo() passes.
        assertThat(result.subscription().discountedAmount())
            .isEqualByComparingTo(BigDecimal.valueOf(1080));
    }

    @Test
    void subscriptionNotFoundCompletesExceptionally() {
        // CF: CompletableFuture.failedFuture(ex) returns an already-failed CF.
        //     Any .thenApply or .thenCompose chained on a failed CF is skipped.
        //     The exception propagates until it reaches an .exceptionally() or .handle() handler.
        //     Since Solution has no error handler, the failure propagates to the caller.
        when(subscriptionService.findById("sub-999"))
            .thenReturn(CompletableFuture.failedFuture(
                new RuntimeException("Subscription not found: sub-999")));

        // .get() re-throws the exception wrapped in ExecutionException.
        // WHY ExecutionException: CompletableFuture wraps all exceptions this way
        //     to carry them across thread boundaries safely.
        assertThatThrownBy(() -> underTest.enrich("sub-999").get())
            .isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class)
            .hasMessageContaining("Subscription not found");
    }
}
```

- [ ] **Step 13: Move question.md into src/main/java path**

```bash
cd /home/brennoz77/LABS/LAB_CLAUDE/PassTheSoftwareEngineeringInterview
git mv algorithms/completable_future/q001_subscription_chain/question.md \
       algorithms/src/main/java/com/interview/algorithms/completable_future/q001_subscription_chain/question.md
```

- [ ] **Step 14: Update question.md content**

Replace `algorithms/src/main/java/com/interview/algorithms/completable_future/q001_subscription_chain/question.md` with:

```markdown
# Exercise 1 — Subscription Chain (Easy)

**CF methods:** `thenApply`, `thenCompose`
**Estimated time:** 20 min
**Your file:** `MyAnswer.java` (this directory — start from scratch)
**Reference:** `solution/Solution.java` (open only after attempting)

---

## Problem

Design and implement an enrichment pipeline for a subscription billing system.

Implement a method with this signature in `MyAnswer.java`:

```java
public CompletableFuture<EnrichedSubscription> enrich(String subscriptionId)
```

The pipeline must:
1. Fetch a `Subscription` by id — async (network/DB call)
2. Apply a pricing discount — **synchronous** transform (pure function, no IO)
3. Fetch the `Account` using the `accountId` from the discounted subscription — async, depends on step 2
4. Combine into an `EnrichedSubscription(discountedSubscription, account)`

## What to create (in this package, from scratch)

You decide the field names and types. Here is what the system needs:

- **`Subscription`** — has at minimum: an `id`, an `accountId`, a plan name, and an amount
- **`DiscountedSubscription`** — same identity as Subscription but with a discounted amount field
- **`Account`** — has at minimum: an `id`, a name, an email
- **`EnrichedSubscription`** — combines DiscountedSubscription + Account
- **`SubscriptionService`** — async interface: `CompletableFuture<Subscription> findById(String id)`
- **`PricingService`** — **synchronous** interface: `DiscountedSubscription applyDiscount(Subscription s)`
- **`AccountService`** — async interface: `CompletableFuture<Account> findById(String accountId)`
- **`MyAnswer`** — constructor takes all three services; implements `enrich()`

## Constraints

- Do **not** call `.get()` or `.join()` inside `enrich()` — chain CFs
- Chain from `subscriptionService.findById(subscriptionId)` using CF methods only

## Key question before coding

`PricingService.applyDiscount()` is synchronous. `AccountService.findById()` is asynchronous.
- Which CF method handles a synchronous transform?
- Which CF method handles an async step that depends on the previous result?
- What type would you get if you used the wrong method for the async step?

## TDD workflow

1. Write `MyAnswerTest.java` in `src/test/java/...q001_subscription_chain/`
2. Use `@Mock` for all three services; stub with `CompletableFuture.completedFuture(...)`
3. Run tests — confirm they fail (Red)
4. Implement `MyAnswer.enrich()` — make them pass (Green)
5. Refactor

## Run your tests

```bash
cd PassTheSoftwareEngineeringInterview
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn test -pl algorithms \
  -Dtest="com.interview.algorithms.completable_future.q001_subscription_chain.MyAnswerTest"
```

## Hints and reference

- Type `hint` in chat for a nudge
- Type `solution` to reveal the reference implementation
- Or open `solution/Solution.java` directly (but try first!)
```

- [ ] **Step 15: Verify Maven compiles**

```bash
cd /home/brennoz77/LABS/LAB_CLAUDE/PassTheSoftwareEngineeringInterview
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn compile test-compile -pl algorithms -q 2>&1 | tail -5
```

Expected: `BUILD SUCCESS`. If you see compilation errors about missing types from the deleted files, the old Solution.java was not fully replaced — re-check Step 8.

- [ ] **Step 16: Commit**

```bash
cd /home/brennoz77/LABS/LAB_CLAUDE/PassTheSoftwareEngineeringInterview
git add algorithms/src/main/java/com/interview/algorithms/completable_future/q001_subscription_chain/
git add algorithms/src/test/java/com/interview/algorithms/completable_future/q001_subscription_chain/
git commit -m "refactor: restructure q001 — solution/ sub-package, empty MyAnswer, SolutionTest"
```

---

## Task 2: Restructure q002_invoice_builder

**Solution package:** `com.interview.algorithms.completable_future.q002_invoice_builder.solution`

- [ ] **Step 1: Create solution/Subscription.java**

```java
package com.interview.algorithms.completable_future.q002_invoice_builder.solution;

import java.math.BigDecimal;

// Java: Java 21 record — immutable value object.
// NOTE: This Subscription has only 3 fields (no accountId) — different from q001.
//       In q002, the accountId is passed separately to buildInvoice() by the caller
//       because the invoice builder fetches billing address and payment method by accountId
//       independently of the subscription lookup.
// WHY: In a real billing system, subscriptions and accounts are separate entities.
//      Keeping accountId out of Subscription here models the scenario where the caller
//      already knows the account (e.g. from session context) and uses it for parallel fetches.
public record Subscription(
    String id,
    String planName,
    // Java: BigDecimal for money — no floating-point precision issues at invoice scale.
    BigDecimal amount  // invoice total before tax
) {}
```

- [ ] **Step 2: Create solution/BillingAddress.java**

```java
package com.interview.algorithms.completable_future.q002_invoice_builder.solution;

// WHY a separate BillingAddress type: billing addresses live in the account service,
//     not the subscription service. Fetching it separately (not embedded in Subscription)
//     allows independent caching and independent service ownership.
// CF implication: BillingAddress fetch is INDEPENDENT of Subscription and PaymentMethod —
//     this is exactly what makes allOf() (not thenCompose) the right tool.
public record BillingAddress(
    String accountId, // links back to the account
    String street,
    String city,
    String postCode
) {}
```

- [ ] **Step 3: Create solution/PaymentMethod.java**

```java
package com.interview.algorithms.completable_future.q002_invoice_builder.solution;

// WHY last4 and not full card number:
//     PCI-DSS (Payment Card Industry Data Security Standard) prohibits storing or
//     transmitting full card numbers in plain text. Only the last 4 digits are safe
//     to display on invoices and UI.
public record PaymentMethod(
    String accountId, // links to the account
    String type,      // e.g. "CARD", "DIRECT_DEBIT", "BANK_TRANSFER"
    String last4      // last 4 digits of card, or last 4 of account number
) {}
```

- [ ] **Step 4: Create solution/Invoice.java**

```java
package com.interview.algorithms.completable_future.q002_invoice_builder.solution;

import java.math.BigDecimal;

// Invoice is an aggregate — it brings together data from three separate services
// (subscription service, billing address service, payment method service).
// Java: compact accessor method `amount()` is defined inline in the record body.
//       Records can have methods — they just cannot have mutable instance fields.
// WHY the amount() accessor: avoids callers having to chain invoice.subscription().amount()
//     in every place they need the total. Encapsulates the navigation.
public record Invoice(
    Subscription subscription,
    BillingAddress billingAddress,
    PaymentMethod paymentMethod
) {
    // Java: compact method inside a record body — valid Java 16+ syntax.
    // Delegates to the subscription's amount field.
    public BigDecimal amount() {
        return subscription.amount();
    }
}
```

- [ ] **Step 5: Create solution/SubscriptionService.java**

```java
package com.interview.algorithms.completable_future.q002_invoice_builder.solution;

import java.util.concurrent.CompletableFuture;

// CF: async — subscription lookup requires a remote call.
// WHY getSubscription (not findById): naming follows the query intent.
//     findById is typically a repository method; getSubscription is a service operation
//     that may apply business rules (e.g. check subscription is active) before returning.
public interface SubscriptionService {
    CompletableFuture<Subscription> getSubscription(String subscriptionId);
}
```

- [ ] **Step 6: Create solution/BillingAddressService.java**

```java
package com.interview.algorithms.completable_future.q002_invoice_builder.solution;

import java.util.concurrent.CompletableFuture;

// CF: async — billing address lives in the account service (remote call).
// KEY: this fetch is INDEPENDENT of the subscription fetch and payment method fetch.
//      All three can fire simultaneously — that's the whole point of this exercise.
public interface BillingAddressService {
    CompletableFuture<BillingAddress> getBillingAddress(String accountId);
}
```

- [ ] **Step 7: Create solution/PaymentMethodService.java**

```java
package com.interview.algorithms.completable_future.q002_invoice_builder.solution;

import java.util.concurrent.CompletableFuture;

// CF: async — payment method stored in a payment vault service (remote call).
// WHY getDefaultPaymentMethod: accounts can have multiple payment methods;
//     this returns whichever is set as primary without requiring the caller to know the ID.
public interface PaymentMethodService {
    CompletableFuture<PaymentMethod> getDefaultPaymentMethod(String accountId);
}
```

- [ ] **Step 8: Create solution/Solution.java**

```java
package com.interview.algorithms.completable_future.q002_invoice_builder.solution;

import java.util.concurrent.CompletableFuture;

// REFERENCE SOLUTION — open only after attempting MyAnswer.java
//
// Demonstrates: parallel independent CF fetches with allOf().
// The key insight: when N async calls are INDEPENDENT (no result depends on another),
// fire ALL of them before waiting for ANY. allOf() coordinates completion.
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

        // WHY fire all three BEFORE waiting for any:
        //     Sequential: sub(300ms) → addr(200ms) → pm(150ms) = 650ms total
        //     Parallel:   all three in flight simultaneously        = ~300ms total (longest wins)
        // These three calls are INDEPENDENT — no result depends on another.
        CompletableFuture<Subscription>   subCf  = subscriptionService.getSubscription(subscriptionId);
        CompletableFuture<BillingAddress> addrCf = billingAddressService.getBillingAddress(accountId);
        CompletableFuture<PaymentMethod>  pmCf   = paymentMethodService.getDefaultPaymentMethod(accountId);

        // CF: allOf(cf1, cf2, cf3) returns a CF<Void> that completes when ALL input CFs complete.
        //     If any CF fails, allOf completes exceptionally immediately.
        //     allOf does NOT carry the results — access them via the original CF references.
        return CompletableFuture.allOf(subCf, addrCf, pmCf)
            .thenApply(ignored -> {
                // CF: join() is SAFE here because allOf() guarantees that subCf, addrCf, pmCf
                //     are ALL done before this thenApply callback runs.
                //     join() on a completed CF returns immediately (no blocking, no waiting).
                //     Never call join() before allOf() — that would block a thread.
                // WHY join() and not get(): join() throws CompletionException (unchecked),
                //     not ExecutionException (checked). Cleaner inside lambda bodies
                //     where checked exceptions must be caught explicitly.
                return new Invoice(subCf.join(), addrCf.join(), pmCf.join());
            });

        // ALT: thenCombine for exactly 2 CFs:
        //   subCf.thenCombine(addrCf, (sub, addr) -> new Object[]{sub, addr})
        //        .thenCombine(pmCf, (pair, pm) -> new Invoice((Subscription)pair[0], (BillingAddress)pair[1], pm))
        //   Works but: (a) ugly intermediate type, (b) doesn't parallelise well beyond 2.
        //   allOf is the idiomatic choice for 3+ independent CFs.
    }
}
```

- [ ] **Step 9: Strip MyAnswer.java to empty shell**

Replace `algorithms/src/main/java/com/interview/algorithms/completable_future/q002_invoice_builder/MyAnswer.java` with:

```java
package com.interview.algorithms.completable_future.q002_invoice_builder;

/**
 * Exercise 2 — Invoice Builder
 *
 * Read question.md before starting.
 *
 * Design your own models, interfaces, and implement the solution from scratch.
 * Follow TDD: write MyAnswerTest.java first, then make it pass here.
 *
 * Reference: solution/ sub-package contains a fully commented implementation
 * — only open it after you have attempted this exercise.
 */
public class MyAnswer {
    // your implementation here
}
```

- [ ] **Step 10: Delete old main-package files**

```bash
cd /home/brennoz77/LABS/LAB_CLAUDE/PassTheSoftwareEngineeringInterview
git rm algorithms/src/main/java/com/interview/algorithms/completable_future/q002_invoice_builder/BillingAddress.java
git rm algorithms/src/main/java/com/interview/algorithms/completable_future/q002_invoice_builder/BillingAddressService.java
git rm algorithms/src/main/java/com/interview/algorithms/completable_future/q002_invoice_builder/Invoice.java
git rm algorithms/src/main/java/com/interview/algorithms/completable_future/q002_invoice_builder/PaymentMethod.java
git rm algorithms/src/main/java/com/interview/algorithms/completable_future/q002_invoice_builder/PaymentMethodService.java
git rm algorithms/src/main/java/com/interview/algorithms/completable_future/q002_invoice_builder/Solution.java
git rm algorithms/src/main/java/com/interview/algorithms/completable_future/q002_invoice_builder/Subscription.java
git rm algorithms/src/main/java/com/interview/algorithms/completable_future/q002_invoice_builder/SubscriptionService.java
git rm algorithms/src/test/java/com/interview/algorithms/completable_future/q002_invoice_builder/MyAnswerTest.java
```

- [ ] **Step 11: Create SolutionTest.java**

Create `algorithms/src/test/java/com/interview/algorithms/completable_future/q002_invoice_builder/solution/SolutionTest.java`:

```java
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

@ExtendWith(MockitoExtension.class)
class SolutionTest {

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
        // results. If paymentMethodService is not stubbed, it returns null → NullPointerException
        // before we even get to the address failure assertion.

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
```

- [ ] **Step 12: Move and update question.md**

```bash
cd /home/brennoz77/LABS/LAB_CLAUDE/PassTheSoftwareEngineeringInterview
git mv algorithms/completable_future/q002_invoice_builder/question.md \
       algorithms/src/main/java/com/interview/algorithms/completable_future/q002_invoice_builder/question.md
```

Replace the content of that file with:

```markdown
# Exercise 2 — Invoice Builder (Medium)

**CF methods:** `allOf` (primary), `thenCombine` (alternative)
**Estimated time:** 25 min
**Your file:** `MyAnswer.java` (this directory — start from scratch)
**Reference:** `solution/Solution.java` (open only after attempting)

---

## Problem

Build an invoice from three independent data sources.

Implement this method in `MyAnswer.java`:

```java
public CompletableFuture<Invoice> buildInvoice(String subscriptionId, String accountId)
```

You need:
- `SubscriptionService.getSubscription(subscriptionId)` → async
- `BillingAddressService.getBillingAddress(accountId)` → async
- `PaymentMethodService.getDefaultPaymentMethod(accountId)` → async

All three calls are **completely independent** — no result depends on another.

## What to create (in this package, from scratch)

- **`Subscription`** — has id, planName, amount (BigDecimal)
- **`BillingAddress`** — has accountId, street, city, postCode
- **`PaymentMethod`** — has accountId, type, and last4 digits
- **`Invoice`** — aggregates all three; add an `amount()` accessor method
- **`SubscriptionService`** — async interface
- **`BillingAddressService`** — async interface
- **`PaymentMethodService`** — async interface
- **`MyAnswer`** — implements `buildInvoice()`

## Constraints

- All three CF calls must start **before** any result is awaited
- Do NOT chain them sequentially — that defeats parallelism
- Test `allThreeFetchesFireBeforeAnyResultIsUsed` in your test suite verifies this

## Key questions before coding

1. `allOf()` returns `CompletableFuture<Void>` — how do you access the results of your 3 CFs after `allOf` completes?
2. When is `join()` safe to call inside a `thenApply` callback?
3. How would `thenCombine` work for this? Where does it get awkward with 3 CFs?

## TDD workflow

1. Write `MyAnswerTest.java` in `src/test/java/...q002_invoice_builder/`
2. Stub all three services with `completedFuture()`
3. Write a parallelism test using `AtomicInteger` + unresolved CFs (see `solution/SolutionTest.java` after you attempt)
4. Red → Green → Refactor

## Run your tests

```bash
cd PassTheSoftwareEngineeringInterview
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn test -pl algorithms \
  -Dtest="com.interview.algorithms.completable_future.q002_invoice_builder.MyAnswerTest"
```

## Hints and reference

- Type `hint` in chat for a nudge
- Type `solution` to reveal the reference implementation
```

- [ ] **Step 13: Verify compilation**

```bash
cd /home/brennoz77/LABS/LAB_CLAUDE/PassTheSoftwareEngineeringInterview
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn compile test-compile -pl algorithms -q 2>&1 | tail -5
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 14: Commit**

```bash
cd /home/brennoz77/LABS/LAB_CLAUDE/PassTheSoftwareEngineeringInterview
git add algorithms/src/main/java/com/interview/algorithms/completable_future/q002_invoice_builder/
git add algorithms/src/test/java/com/interview/algorithms/completable_future/q002_invoice_builder/
git commit -m "refactor: restructure q002 — solution/ sub-package, empty MyAnswer, SolutionTest"
```

---

## Task 3: Restructure q003_batch_renewal

**Solution package:** `com.interview.algorithms.completable_future.q003_batch_renewal.solution`

- [ ] **Step 1: Create solution/SubscriptionPlan.java**

```java
package com.interview.algorithms.completable_future.q003_batch_renewal.solution;

import java.math.BigDecimal;

// WHY SubscriptionPlan (not Subscription): in a billing context, a plan is the
//     contract details (what the customer signed up for and how much to charge).
//     Distinguishing Plan from Subscription avoids the ambiguity of "which amount?" —
//     the plan's amount IS what gets charged at renewal.
public record SubscriptionPlan(
    String subscriptionId,   // which subscription this plan belongs to
    String planName,         // e.g. "MONTHLY", "ANNUAL"
    BigDecimal amount        // Java: BigDecimal — amount to charge at renewal
) {}
```

- [ ] **Step 2: Create solution/PaymentMethod.java**

```java
package com.interview.algorithms.completable_future.q003_batch_renewal.solution;

// NOTE: This PaymentMethod differs from q002's: field is 'token' not 'last4'.
// WHY token: in batch renewal, the charge is made programmatically against a
//     stored payment token (e.g. Stripe token, vault reference).
//     The token allows charging without re-entering card details.
//     It is never the raw card number — tokenisation is a PCI-DSS requirement.
public record PaymentMethod(
    String subscriptionId,  // which subscription this payment method serves
    String type,            // e.g. "CARD", "SEPA_DIRECT_DEBIT"
    String token            // opaque payment vault reference — never a real card number
) {}
```

- [ ] **Step 3: Create solution/ChargeResult.java**

```java
package com.interview.algorithms.completable_future.q003_batch_renewal.solution;

import java.math.BigDecimal;

// Returned by ChargeService when a charge succeeds.
// WHY chargeId: the payment processor (Stripe, Adyen, etc.) returns a charge ID
//     that can be used for refunds, disputes, and audit trail.
//     Always persist this — losing it means you cannot refund programmatically.
public record ChargeResult(
    String subscriptionId,  // identifies which renewal this charge belongs to
    String chargeId,        // processor-assigned charge identifier (e.g. "ch_1ABC...")
    BigDecimal amount       // actual amount charged (should match plan amount)
) {}
```

- [ ] **Step 4: Create solution/RenewalResult.java**

```java
package com.interview.algorithms.completable_future.q003_batch_renewal.solution;

// RenewalResult wraps the outcome of renewing ONE subscription.
// WHY not throw on failure: in a batch renewal, one failed charge must NOT stop
//     the others. Using a result type (success flag + optional error) instead of
//     exceptions allows the caller to collect ALL outcomes, not just the first failure.
// Pattern: this is the Result/Either pattern — a discriminated union of success/failure.
//     In Haskell/Scala this would be Either<Error, ChargeResult>.
//     In Java, we model it as a record with a boolean flag and nullable fields.
public record RenewalResult(
    String subscriptionId,
    boolean success,
    ChargeResult chargeResult,  // non-null when success == true
    String errorMessage         // non-null when success == false
) {

    // Java: static factory methods on records are valid and preferred over 'new'.
    //       They communicate intent clearly at the call site.

    // WHY static factory over constructor: the constructor requires all 4 args every time.
    //     With factory methods, successful results never have to pass null for errorMessage,
    //     and failed results never have to pass null for chargeResult.
    //     The correct null is encapsulated in the factory — callers cannot get it wrong.
    public static RenewalResult success(String subscriptionId, ChargeResult chargeResult) {
        return new RenewalResult(subscriptionId, true, chargeResult, null);
    }

    public static RenewalResult failed(String subscriptionId, String errorMessage) {
        return new RenewalResult(subscriptionId, false, null, errorMessage);
    }
}
```

- [ ] **Step 5: Create solution/PlanService.java**

```java
package com.interview.algorithms.completable_future.q003_batch_renewal.solution;

import java.util.concurrent.CompletableFuture;

// CF: async — plan data comes from the subscription/billing service (network call).
// WHY per-subscription: each subscription may be on a different plan with a different
//     price. Batch renewal must fetch the current plan for each ID individually.
public interface PlanService {
    CompletableFuture<SubscriptionPlan> getPlan(String subscriptionId);
}
```

- [ ] **Step 6: Create solution/PaymentMethodService.java**

```java
package com.interview.algorithms.completable_future.q003_batch_renewal.solution;

import java.util.concurrent.CompletableFuture;

// CF: async — payment tokens are stored in a payment vault (external service).
// KEY: PlanService and PaymentMethodService calls for the SAME subscriptionId
//      are INDEPENDENT of each other — they can run in parallel using thenCombine.
public interface PaymentMethodService {
    CompletableFuture<PaymentMethod> getPaymentMethod(String subscriptionId);
}
```

- [ ] **Step 7: Create solution/ChargeService.java**

```java
package com.interview.algorithms.completable_future.q003_batch_renewal.solution;

import java.util.concurrent.CompletableFuture;

// CF: async — charging requires a call to the payment processor (Stripe, Adyen, etc.).
// WHY thenCompose (not thenApply) in the pipeline:
//     charge() returns CompletableFuture<ChargeResult>.
//     If we used thenApply to call charge(), we'd get CF<CF<ChargeResult>>.
//     thenCompose "flattens" the nested CF: CF<CF<ChargeResult>> → CF<ChargeResult>.
// ORDERING: charge MUST come after both plan and payment method are known.
//           plan + paymentMethod are fetched in parallel → combined → then charge.
public interface ChargeService {
    CompletableFuture<ChargeResult> charge(SubscriptionPlan plan, PaymentMethod paymentMethod);
}
```

- [ ] **Step 8: Create solution/Solution.java**

```java
package com.interview.algorithms.completable_future.q003_batch_renewal.solution;

import java.util.List;
import java.util.concurrent.CompletableFuture;

// REFERENCE SOLUTION — open only after attempting MyAnswer.java
//
// Demonstrates ALL FIVE CF methods:
//   thenCombine  — parallel fetch of plan + paymentMethod per subscription
//   thenCompose  — flattening the async charge call
//   thenApply    — wrapping charge result in RenewalResult.success()
//   exceptionally — per-item failure isolation (NOT on the outer batch)
//   allOf        — waiting for ALL per-item pipelines to complete
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

        // Java: Stream.map() here transforms each subscriptionId into a CF<RenewalResult>.
        //       All CFs start immediately — no blocking, no waiting.
        //       .toList() collects ALL CFs into a list before any join.
        // WHY collect first: if we called allOf() inside the stream, it would not have
        //     all futures yet. Collecting first ensures all per-item pipelines are in flight.
        List<CompletableFuture<RenewalResult>> futures = subscriptionIds.stream()
            .map(this::renewOne)  // each call returns CF<RenewalResult> immediately
            .toList();

        // CF: allOf() waits for EVERY CF in the list to complete — success OR failure.
        //     Because renewOne() handles failures with exceptionally(), every CF in 'futures'
        //     will complete normally (never exceptionally) — allOf() will always succeed.
        // Java: futures.toArray(new CompletableFuture[0]) — the zero-length array is a
        //       type hint for toArray(); Java uses it to determine the array type at runtime.
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(ignored ->
                // CF: join() is safe here — allOf() guarantees every future is done.
                futures.stream()
                    .map(CompletableFuture::join)  // method reference — same as cf -> cf.join()
                    .toList()
            );
    }

    private CompletableFuture<RenewalResult> renewOne(String subscriptionId) {

        // Java: local record (Java 16+) — a record defined inside a method.
        //       Useful for intermediate types that don't belong in the public API.
        //       PlanAndPayment holds both fetched objects so we can pass them to charge().
        record PlanAndPayment(SubscriptionPlan plan, PaymentMethod paymentMethod) {}

        // Both fetches are INDEPENDENT for the same subscriptionId.
        CompletableFuture<SubscriptionPlan> planCf = planService.getPlan(subscriptionId);
        CompletableFuture<PaymentMethod>    pmCf   = paymentMethodService.getPaymentMethod(subscriptionId);

        return planCf
            // CF: thenCombine(other, fn) — waits for BOTH planCf AND pmCf to complete,
            //     then passes both results to fn. The two fetches run in parallel.
            // WHY thenCombine here (not allOf): we have exactly 2 independent CFs and need
            //     both values in the next step. thenCombine is ergonomic for exactly 2.
            //     allOf would also work but requires join() to extract the values.
            .thenCombine(pmCf, PlanAndPayment::new)  // CF<SubscriptionPlan> + CF<PaymentMethod> → CF<PlanAndPayment>

            // CF: thenCompose — chargeService.charge() returns CF<ChargeResult>.
            //     If we used thenApply here, the return type would be CF<CF<ChargeResult>>.
            //     thenCompose "flattens" this: the inner CF replaces the outer, giving CF<ChargeResult>.
            .thenCompose(pp -> chargeService.charge(pp.plan(), pp.paymentMethod()))  // CF<PlanAndPayment> → CF<ChargeResult>

            // CF: thenApply — sync wrap of successful charge into RenewalResult.success().
            .thenApply(charge -> RenewalResult.success(subscriptionId, charge))  // CF<ChargeResult> → CF<RenewalResult>

            // CF: exceptionally — catches any exception in the pipeline ABOVE this line.
            //     Returns a completed-normally CF<RenewalResult> with a failed result.
            // WHY PER-ITEM (not on the outer allOf):
            //     If exceptionally were on the allOf(), a single charge failure would produce
            //     ONE RenewalResult covering the whole batch — wrong.
            //     Here, each subscription's CF handles its own failure independently.
            //     The batch always gets back a full list, regardless of individual failures.
            .exceptionally(ex -> RenewalResult.failed(subscriptionId, ex.getMessage()));
    }
}
```

- [ ] **Step 9: Strip MyAnswer.java to empty shell**

Replace `algorithms/src/main/java/com/interview/algorithms/completable_future/q003_batch_renewal/MyAnswer.java` with:

```java
package com.interview.algorithms.completable_future.q003_batch_renewal;

/**
 * Exercise 3 — Batch Renewal
 *
 * Read question.md before starting.
 *
 * Design your own models, interfaces, and implement the solution from scratch.
 * Follow TDD: write MyAnswerTest.java first, then make it pass here.
 *
 * Reference: solution/ sub-package contains a fully commented implementation
 * — only open it after you have attempted this exercise.
 */
public class MyAnswer {
    // your implementation here
}
```

- [ ] **Step 10: Delete old main-package files and test**

```bash
cd /home/brennoz77/LABS/LAB_CLAUDE/PassTheSoftwareEngineeringInterview
git rm algorithms/src/main/java/com/interview/algorithms/completable_future/q003_batch_renewal/ChargeResult.java
git rm algorithms/src/main/java/com/interview/algorithms/completable_future/q003_batch_renewal/ChargeService.java
git rm algorithms/src/main/java/com/interview/algorithms/completable_future/q003_batch_renewal/PaymentMethod.java
git rm algorithms/src/main/java/com/interview/algorithms/completable_future/q003_batch_renewal/PaymentMethodService.java
git rm algorithms/src/main/java/com/interview/algorithms/completable_future/q003_batch_renewal/PlanService.java
git rm algorithms/src/main/java/com/interview/algorithms/completable_future/q003_batch_renewal/RenewalResult.java
git rm algorithms/src/main/java/com/interview/algorithms/completable_future/q003_batch_renewal/Solution.java
git rm algorithms/src/main/java/com/interview/algorithms/completable_future/q003_batch_renewal/SubscriptionPlan.java
git rm algorithms/src/test/java/com/interview/algorithms/completable_future/q003_batch_renewal/MyAnswerTest.java
```

- [ ] **Step 11: Create SolutionTest.java**

Create `algorithms/src/test/java/com/interview/algorithms/completable_future/q003_batch_renewal/solution/SolutionTest.java`:

```java
package com.interview.algorithms.completable_future.q003_batch_renewal.solution;

// REFERENCE TESTS — open only after writing your own MyAnswerTest.java
//
// Key test to understand: oneChargeFailureDoesNotFailEntireBatch.
// This is the critical behaviour of exercise 3 — failure isolation per item.
// Your test suite MUST include a test like this.

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolutionTest {

    @Mock PlanService planService;
    @Mock PaymentMethodService paymentMethodService;
    @Mock ChargeService chargeService;

    Solution underTest;

    @BeforeEach
    void setUp() {
        underTest = new Solution(planService, paymentMethodService, chargeService);
    }

    @Test
    void allSubscriptionsRenewedSuccessfully() throws Exception {
        stubSuccessfulRenewal("sub-1", "charge-1");
        stubSuccessfulRenewal("sub-2", "charge-2");
        stubSuccessfulRenewal("sub-3", "charge-3");

        List<RenewalResult> results = underTest.processRenewals(List.of("sub-1", "sub-2", "sub-3")).get();

        assertThat(results).hasSize(3);
        // AssertJ: allMatch checks a predicate holds for every element in the list.
        assertThat(results).allMatch(RenewalResult::success);
        // containsExactlyInAnyOrder: all three IDs present, order not guaranteed (allOf has no order guarantee).
        assertThat(results).extracting(RenewalResult::subscriptionId)
            .containsExactlyInAnyOrder("sub-1", "sub-2", "sub-3");
    }

    @Test
    void oneChargeFailureDoesNotFailEntireBatch() throws Exception {
        // CRITICAL TEST: this is the core requirement of exercise 3.
        // The returned CF must complete NORMALLY (not exceptionally) even when one charge fails.
        // The failed item appears in the list with success==false.
        stubSuccessfulRenewal("sub-1", "charge-1");

        // sub-2 charge returns a failed CF — simulates card decline
        stubPlanAndPayment("sub-2");
        when(chargeService.charge(
                new SubscriptionPlan("sub-2", "MONTHLY", BigDecimal.valueOf(50)),
                new PaymentMethod("sub-2", "CARD", "token-sub-2")))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Card declined for sub-2")));

        stubSuccessfulRenewal("sub-3", "charge-3");

        // .get() — if the overall CF failed, this would throw ExecutionException.
        // The fact that it doesn't throw proves the failure was isolated to sub-2.
        List<RenewalResult> results = underTest.processRenewals(List.of("sub-1", "sub-2", "sub-3")).get();

        assertThat(results).hasSize(3);  // batch size unchanged
        assertThat(results).filteredOn(r -> r.subscriptionId().equals("sub-1")).allMatch(RenewalResult::success);
        assertThat(results).filteredOn(r -> r.subscriptionId().equals("sub-2")).allMatch(r -> !r.success());
        assertThat(results).filteredOn(r -> r.subscriptionId().equals("sub-2"))
            .extracting(RenewalResult::errorMessage)
            .containsExactly("Card declined for sub-2");
        assertThat(results).filteredOn(r -> r.subscriptionId().equals("sub-3")).allMatch(RenewalResult::success);
    }

    @Test
    void planAndPaymentFetchedBeforeCharge() throws Exception {
        // Verifies the per-item pipeline order: plan + payment THEN charge.
        // Mockito's verify() confirms chargeService received the exact objects from stubs.
        stubSuccessfulRenewal("sub-1", "charge-1");

        underTest.processRenewals(List.of("sub-1")).get();

        verify(planService).getPlan("sub-1");
        verify(paymentMethodService).getPaymentMethod("sub-1");
        verify(chargeService).charge(
            new SubscriptionPlan("sub-1", "MONTHLY", BigDecimal.valueOf(50)),
            new PaymentMethod("sub-1", "CARD", "token-sub-1")
        );
    }

    @Test
    void resultSizeMatchesInputSize() throws Exception {
        stubSuccessfulRenewal("sub-1", "charge-1");
        stubSuccessfulRenewal("sub-2", "charge-2");

        List<RenewalResult> results = underTest.processRenewals(List.of("sub-1", "sub-2")).get();

        assertThat(results).hasSize(2);
    }

    // ---- test helpers ----

    // WHY extract to helper methods: avoids repeating 6 lines of stubbing per test.
    //     DRY test helpers make tests read as intent (WHAT) not mechanics (HOW).
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

- [ ] **Step 12: Move and update question.md**

```bash
cd /home/brennoz77/LABS/LAB_CLAUDE/PassTheSoftwareEngineeringInterview
git mv algorithms/completable_future/q003_batch_renewal/question.md \
       algorithms/src/main/java/com/interview/algorithms/completable_future/q003_batch_renewal/question.md
```

Replace the content with:

```markdown
# Exercise 3 — Batch Subscription Renewal (Hard)

**CF methods:** `thenCombine`, `thenCompose`, `thenApply`, `exceptionally`, `allOf`
**Estimated time:** 35 min
**Your file:** `MyAnswer.java` (this directory — start from scratch)
**Reference:** `solution/Solution.java` (open only after attempting)

---

## Problem

Implement batch renewal for a list of subscriptions.

Implement this method in `MyAnswer.java`:

```java
public CompletableFuture<List<RenewalResult>> processRenewals(List<String> subscriptionIds)
```

You need:
- `PlanService.getPlan(subscriptionId)` → async
- `PaymentMethodService.getPaymentMethod(subscriptionId)` → async
- `ChargeService.charge(plan, paymentMethod)` → async, depends on plan + payment

For **each** subscriptionId:
1. Fetch `SubscriptionPlan` and `PaymentMethod` **in parallel**
2. Once both are ready, charge via `ChargeService`
3. Wrap success as `RenewalResult.success(subscriptionId, chargeResult)`
4. If charge fails, produce `RenewalResult.failed(subscriptionId, errorMessage)` — do NOT propagate the exception

Return `CompletableFuture<List<RenewalResult>>` that completes when ALL renewals are done.

## What to create (in this package, from scratch)

- **`SubscriptionPlan`** — has subscriptionId, planName, amount (BigDecimal)
- **`PaymentMethod`** — has subscriptionId, type, and a payment token
- **`ChargeResult`** — has subscriptionId, chargeId, amount
- **`RenewalResult`** — has subscriptionId, success flag, chargeResult (nullable), errorMessage (nullable)
  - Add static factories: `success(id, chargeResult)` and `failed(id, errorMessage)`
- **`PlanService`**, **`PaymentMethodService`**, **`ChargeService`** — async interfaces
- **`MyAnswer`** — implements `processRenewals()`

## Critical constraint

**One failed charge must NOT fail the whole batch.**
The returned CF must complete normally with a full list where failed items have `success == false`.

This is the key difference from exercise 2 (one failure = whole invoice CF fails).

## Key questions before coding

1. For each subscription: plan and payment method are independent. Which CF method combines two independent CFs into one callback that receives both results?
2. `chargeService.charge()` returns `CF<ChargeResult>`. If you use `thenApply` to call it, what type do you get? How does `thenCompose` fix this?
3. `exceptionally` must be on which CF — the per-item one or the outer batch one? What goes wrong if you put it on the outer one?
4. Why must you collect all per-item CFs into a List BEFORE calling `allOf`?

## TDD workflow

Break into two methods: `processRenewals()` (outer batch) and `renewOne()` (per-item pipeline). Test each concept separately:
- Test 1: all succeed
- Test 2: one fails, others succeed, list still full (most important test)
- Test 3: charge called with correct plan + payment
- Test 4: result list size equals input size

## Run your tests

```bash
cd PassTheSoftwareEngineeringInterview
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn test -pl algorithms \
  -Dtest="com.interview.algorithms.completable_future.q003_batch_renewal.MyAnswerTest"
```

## Hints and reference

- Type `hint` in chat for a nudge
- Type `solution` to reveal the reference implementation
```

- [ ] **Step 13: Verify compilation**

```bash
cd /home/brennoz77/LABS/LAB_CLAUDE/PassTheSoftwareEngineeringInterview
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn compile test-compile -pl algorithms -q 2>&1 | tail -5
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 14: Commit**

```bash
cd /home/brennoz77/LABS/LAB_CLAUDE/PassTheSoftwareEngineeringInterview
git add algorithms/src/main/java/com/interview/algorithms/completable_future/q003_batch_renewal/
git add algorithms/src/test/java/com/interview/algorithms/completable_future/q003_batch_renewal/
git commit -m "refactor: restructure q003 — solution/ sub-package, empty MyAnswer, SolutionTest"
```

---

## Task 4: Clean up and verify

- [ ] **Step 1: Remove now-empty algorithms/completable_future/ directory**

```bash
cd /home/brennoz77/LABS/LAB_CLAUDE/PassTheSoftwareEngineeringInterview
rmdir algorithms/completable_future/q001_subscription_chain \
      algorithms/completable_future/q002_invoice_builder \
      algorithms/completable_future/q003_batch_renewal \
      algorithms/completable_future
git status | grep completable_future
```

Expected: no untracked files for `algorithms/completable_future/` — directory is gone.

- [ ] **Step 2: Run SolutionTests to confirm solution/ files are correct**

```bash
cd /home/brennoz77/LABS/LAB_CLAUDE/PassTheSoftwareEngineeringInterview
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn test -pl algorithms \
  -Dtest="**/completable_future/**/solution/SolutionTest" 2>&1 | grep -E "Tests run|BUILD"
```

Expected:
```
Tests run: 4, Failures: 0, Errors: 0  (q001 SolutionTest)
Tests run: 4, Failures: 0, Errors: 0  (q002 SolutionTest)
Tests run: 4, Failures: 0, Errors: 0  (q003 SolutionTest)
BUILD SUCCESS
```

All SolutionTests must PASS — they test Solution.java which is a complete implementation.

- [ ] **Step 3: Confirm no pre-generated tests exist in main packages**

```bash
find /home/brennoz77/LABS/LAB_CLAUDE/PassTheSoftwareEngineeringInterview/algorithms/src/test \
     -path "*/completable_future/*" -name "*.java" | grep -v "solution/" | sort
```

Expected: empty output. Only `solution/SolutionTest.java` files should exist in test/.

- [ ] **Step 4: Confirm MyAnswer.java files are empty shells**

```bash
grep -r "throw new UnsupportedOperationException\|thenApply\|thenCompose\|thenCombine\|allOf" \
  /home/brennoz77/LABS/LAB_CLAUDE/PassTheSoftwareEngineeringInterview/algorithms/src/main/java/com/interview/algorithms/completable_future/q001_subscription_chain/MyAnswer.java \
  /home/brennoz77/LABS/LAB_CLAUDE/PassTheSoftwareEngineeringInterview/algorithms/src/main/java/com/interview/algorithms/completable_future/q002_invoice_builder/MyAnswer.java \
  /home/brennoz77/LABS/LAB_CLAUDE/PassTheSoftwareEngineeringInterview/algorithms/src/main/java/com/interview/algorithms/completable_future/q003_batch_renewal/MyAnswer.java
```

Expected: empty output. None of those methods/calls should appear in MyAnswer.

- [ ] **Step 5: Commit cleanup**

```bash
cd /home/brennoz77/LABS/LAB_CLAUDE/PassTheSoftwareEngineeringInterview
git add -A
git commit -m "chore: remove empty algorithms/completable_future/ directory after question.md move"
```

---

## Self-Review

**Spec coverage:**
- ✅ `solution/` sub-package per exercise — Tasks 1, 2, 3
- ✅ All models in `solution/` with WHY/CF/Java/ALT comments — Tasks 1–3 Steps 1–7
- ✅ `Solution.java` in `solution/` with inline explanatory comments — Tasks 1–3 Step 8
- ✅ `SolutionTest.java` in `test/solution/` with teaching comments — Tasks 1–3 Step 11/12
- ✅ `MyAnswer.java` stripped to empty shell — Tasks 1–3 Step 9
- ✅ Old models/interfaces/MyAnswerTest deleted — Tasks 1–3 Steps 10–11
- ✅ `question.md` moved to `src/main/java/` path and rewritten (no references to pre-generated files) — Tasks 1–3 Step 13–14
- ✅ Maven compiles after each task — Tasks 1–3 Step 15/13
- ✅ Old `algorithms/completable_future/` directory removed — Task 4 Step 1
- ✅ SolutionTests all pass — Task 4 Step 2

**Placeholder scan:** No TBD, no "implement later". All code blocks are complete.

**Type consistency:** All types in `solution/` sub-packages use the `solution` package suffix. SolutionTest imports from the same `solution` package. Zero cross-package imports between MyAnswer (main package) and solution/ files.
