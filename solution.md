# Refactoring Solution

## Context

- **Assessment / original problem:** [Briefly describe the program and the main issues addressed.]
- **Languages used:** Java and C#
- **Approach:** I used Java to express the refactoring using my stronger language experience, then also attempted a C# refactoring of the provided program.

## Java solution

### Refactoring summary

The Java solution uses Spring-managed components and constructor injection to separate the order workflow from persistence, payment, and email delivery. `OrderService` coordinates processing; Spring Data repositories handle product and order access; payment handling is split into implementations of `PaymentStrategy`, selected by `PaymentFactory`; and a transactional event listener sends confirmation email after the order transaction commits. The request is represented by the `ProcessRequest` record, and monetary values use `BigDecimal`.

### Patterns and design principles

| Pattern / principle | Where it is used | Why it was chosen |
|---|---|---|
| Strategy | `PaymentStrategy` and the credit-card, PayPal, and bank-transfer implementations | Encapsulates payment-method-specific behavior behind one contract. |
| Simple factory / registry | `PaymentFactory` | Maps each payment method to its Spring-provided strategy so `OrderService` does not select implementations with a payment-method conditional. |
| Single Responsibility Principle | `OrderService`, repositories, payment strategies, `OrderEventListener`, and `EmailClient` | Separates order coordination, persistence, payment execution, event handling, and mail delivery. |
| Dependency injection | Spring components and constructor-injected dependencies | Keeps service collaborators explicit and lets Spring manage their lifecycle. |
| Transactional event | `OrderService` publishes `OrderSuccessDto`; `OrderEventListener` listens after commit | Moves confirmation email work out of the main order-processing method and runs it after the transaction commits. |
| Request object / record | `ProcessRequest` | Groups the order-processing inputs into one immutable request value. |

### Architecture and flow

`OrderService.doProcess` looks up each requested product, calculates a total and applies the supplied discount, then asks `PaymentFactory` for the strategy matching the request's payment method. If payment returns a success-like result or `"pending"`, the service saves an order and publishes an `OrderSuccessDto`. After the transaction commits, `OrderEventListener` retrieves the saved order and asks `EmailClient` to send the confirmation. `findHistory` loads orders by customer ID and formats them for display.

The service and collaborators are wired by Spring using constructor injection. Spring Data JPA provides the repository implementations.

### Trade-offs and limitations

- Payment outcomes are still represented and checked as strings. Bank transfer returns `"pending"`, but the order is persisted with `COMPLETED`; this behavior should be clarified and modeled explicitly.
- Missing products are silently skipped when calculating the total. The discount condition and calculation also need clarification: the code applies it only when the value is greater than one, and subtracts `total * discount`.
- The email listener runs after transaction commit, but failures are wrapped in a runtime exception and there is no retry or durable delivery mechanism shown. Payment failures, unknown products, discount boundaries, and email failure would benefit from dedicated tests.
- With more time, I would define explicit payment/order result types and business rules, validate missing products and discounts, and add focused tests for the service, strategies, and event listener. The included test currently checks only that the Spring application context loads.

## C# solution

### Refactoring summary

The C# version was refactored incrementally to separate the main responsibilities in the original `OrderProcessor`. `OrderRepository` handles product-price lookup, order persistence, and history retrieval; `PaymentProcessor` handles payment-method-specific calls; and `OrderConfirmationEmailSender` handles confirmation email. `OrderProcessor` coordinates those operations and calculates the discounted total. Its `DoProcess` method accepts the existing `ProcessRequest` class instead of a list of individual arguments.

### Patterns and design principles

| Pattern / principle | Where it is used | Why it was chosen |
|---|---|---|
| Single Responsibility Principle | `OrderRepository`, `PaymentProcessor`, `OrderConfirmationEmailSender` | Moves database, payment, and email work out of the coordinator into focused classes. |
| Repository-style separation | `OrderRepository` | Groups database operations behind a class so persistence is not embedded in the order workflow. |
| Request object | `ProcessRequest` and `OrderProcessor.DoProcess` | Groups the customer, items, payment method, and discount into one method argument. |

### Architecture and flow

`OrderProcessor.DoProcess` gets each item's price through `OrderRepository`, calculates the total and discount, and delegates payment to `PaymentProcessor`. On a successful or pending payment result, it saves the order through the repository and asks `OrderConfirmationEmailSender` to send the email. `FindHistory` delegates history retrieval to the repository.

The classes are separated by responsibility, but the processor currently constructs concrete dependencies directly. This is a simple separation, not a fully configurable or dependency-injected architecture.

### C#-specific decisions

I kept the implementation close to the provided C# code and used familiar, straightforward class and method boundaries rather than introducing a framework or a more elaborate abstraction. `using` blocks dispose database connections and the history reader. The existing `System.Data.SqlClient`, `WebClient`, and `SmtpClient` APIs remain in use; I have not tried to modernize those choices as part of this focused refactor.

### Trade-offs and limitations

- The existing payment-result strings, response messages, and workflow behavior were largely kept as-is. The payment implementations are separated into one class but still selected with conditionals.
- With more time, I would add tests and inject dependencies to make the workflow easier to test. I would also parameterize SQL, move connection and email credentials into configuration/secrets, and review payment status handling. These are known limitations, not addressed by this SRP-focused change.