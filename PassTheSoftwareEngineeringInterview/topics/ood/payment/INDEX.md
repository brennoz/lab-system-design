# payment — Lesson Index

| File | Concepts covered |
|---|---|
| [0001-payment-gateway.html](0001-payment-gateway.html) | Money value object (BigDecimal + Currency), Builder pattern (PaymentRequest), State machine (PaymentStatus transitions), Strategy (PaymentGateway interface), Service Layer, idempotency key (merchantReference), PCI-DSS tokenisation, BigDecimal vs double |
| [0002-payment-wallet.html](0002-payment-wallet.html) | Wallet aggregate, optimistic locking (@Version / CAS), BigDecimal precision, idempotency key on debit/credit, WalletTransaction value object, InsufficientFundsException, OptimisticLockException, pessimistic vs optimistic lock tradeoff, DynamoDB conditional writes |
