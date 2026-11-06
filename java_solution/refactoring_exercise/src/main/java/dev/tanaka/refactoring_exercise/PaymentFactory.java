package dev.tanaka.refactoring_exercise;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentFactory {

    private final Map<Order.PaymentMethod, PaymentStrategy> strategies = new HashMap<>();

    @Autowired
    public PaymentFactory(List<PaymentStrategy> strategies) {
        strategies.forEach(strategy ->
            this.strategies.put(strategy.getSupportedPaymentMethod(), strategy));
    }

    public PaymentStrategy getStrategy(Order.PaymentMethod method) {
        PaymentStrategy strategy = strategies.get(method);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy found for method: " + method);
        }
        return strategy;
    }
}
