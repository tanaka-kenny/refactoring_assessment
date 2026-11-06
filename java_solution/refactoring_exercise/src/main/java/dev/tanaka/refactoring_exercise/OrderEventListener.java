package dev.tanaka.refactoring_exercise;

import jakarta.mail.MessagingException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrderEventListener {

    private final EmailClient client;
    private final OrderRepository orderRepository;

    public OrderEventListener(EmailClient client, OrderRepository orderRepository) {
        this.client = client;
        this.orderRepository = orderRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderSuccess(OrderSuccessDto dto) {

        orderRepository.findById(dto.orderId()).ifPresent(
            order -> {
                try {
                    client.sendEmail(
                        dto.email(),
                        "Order Confirmation",
                        "Your order has been placed. Total: $" + order.getTotal()
                    );
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
            }
        );

    }

}
