package dev.tanaka.refactoring_exercise;

import java.math.BigDecimal;

public interface PaymentStrategy {

    Order.PaymentMethod getSupportedPaymentMethod();
    String execute(BigDecimal amount);
}
