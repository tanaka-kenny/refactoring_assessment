package dev.tanaka.refactoring_exercise;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;

@Component
public class PaymentStrategyPayPalImpl implements PaymentStrategy {

    private final RestClient restClient;

    public PaymentStrategyPayPalImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Order.PaymentMethod getSupportedPaymentMethod() {
        return Order.PaymentMethod.PAY_PAL;
    }

    @Override
    public String execute(BigDecimal amount) throws RestClientException {
        return restClient.get()
            .uri("https://api.paypal.com/charge?amount=" + amount)
            .retrieve()
            .body(String.class);

    }
}
