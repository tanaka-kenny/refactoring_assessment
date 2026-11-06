package dev.tanaka.refactoring_exercise;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;

@Component
public class PaymentStrategyCreditCardImpl implements PaymentStrategy {

    private final RestClient restClient;

    public PaymentStrategyCreditCardImpl(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Order.PaymentMethod getSupportedPaymentMethod() {
        return Order.PaymentMethod.CREDIT_CARD;
    }

    @Override
    public String execute(BigDecimal amount) throws RestClientException {
        return restClient.get()
            .uri("https://api.payment.com/charge?amount=" + amount)
            .retrieve()
            .body(String.class);

    }
}
