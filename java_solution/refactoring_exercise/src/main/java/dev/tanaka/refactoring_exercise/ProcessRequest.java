package dev.tanaka.refactoring_exercise;

import java.math.BigDecimal;
import java.util.List;

public record ProcessRequest(
    Integer customerId,
    String customerEmail,
    List<String> items,
    Order.PaymentMethod paymentMethod,
    BigDecimal discount
) {
}
