package dev.tanaka.refactoring_exercise;

import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final PaymentFactory paymentFactory;
    private final ApplicationEventPublisher eventPublisher;

    public OrderService(ProductRepository productRepository, OrderRepository orderRepository, PaymentFactory paymentFactory, ApplicationEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.paymentFactory = paymentFactory;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public String doProcess(int id, String c_email, List<String> i, Order.PaymentMethod method, BigDecimal disc) {

        BigDecimal total = BigDecimal.ZERO;
        for (String item : i) {
            Optional<Product> byName = productRepository.findByName(item);
            if (byName.isPresent()) {
                Product product = byName.get();
                total = product.getPrice().add(total);
            }
        }

        if (disc.compareTo(BigDecimal.ONE) > 0) {
            total = total.subtract((total.multiply(disc)));
        }

        PaymentStrategy paymentStrategy = paymentFactory.getStrategy(method);
        String paymentResult = paymentStrategy.execute(total);


        if (paymentResult.contains("success") || paymentResult.equals("pending")) {
            Order order = new Order(id, total, Order.OrderStatus.COMPLETED, paymentStrategy.getSupportedPaymentMethod());
            Order saved = orderRepository.save(order);

            eventPublisher.publishEvent(new OrderSuccessDto(c_email, saved.getId()));
            return "Order place successfully.";
        }

        return "Payment failed";
    }

    public List<String> findHistory(String customerId)
    {
        return orderRepository.findByCustomerId(customerId).stream()
                .map(order -> "Order #" + order.getId() + " - $" + order.getTotal() + " - " + order.getStatus())
                .toList();
    }

}
