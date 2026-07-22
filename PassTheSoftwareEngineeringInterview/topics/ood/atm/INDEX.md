# atm — Lesson Index

| File | Concepts covered |
|---|---|
| [0001-atm-domain-model.html](0001-atm-domain-model.html) | State Pattern (card session lifecycle: NO_CARD→CARD_INSERTED→AUTHENTICATED, ejectCard legal from two states), Command Pattern (AtmCommand: WithdrawCommand/DepositCommand/CheckBalanceCommand — first Command-pattern question in this project, contrasted explicitly against Strategy), guard placement discipline (authentication in AtmService, overdraft in Account, never both in the same place), BigDecimal for monetary values, custom InsufficientFundsException — full solution-walkthrough, second question built under the new standing OOD convention |
