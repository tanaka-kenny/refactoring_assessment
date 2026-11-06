package dev.tanaka.refactoring_exercise;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;

@Component
public class PaymentStrategyBankTransferImpl implements PaymentStrategy {

    @Override
    public Order.PaymentMethod getSupportedPaymentMethod() {
        return Order.PaymentMethod.BANK_TRANSFER;
    }

    @Override
    public String execute(BigDecimal amount) throws RestClientException {
        return "pending";
    }
}
