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
    public String doProcess(ProcessRequest request) {

        BigDecimal total = BigDecimal.ZERO;
        for (String item : request.items()) {
            Optional<Product> byName = productRepository.findByName(item);
            if (byName.isPresent()) {
                Product product = byName.get();
                total = product.getPrice().add(total);
            }
        }

        if (request.discount().compareTo(BigDecimal.ONE) > 0) {
            total = total.subtract((total.multiply(request.discount())));
        }

        PaymentStrategy paymentStrategy = paymentFactory.getStrategy(request.paymentMethod());
        String paymentResult = paymentStrategy.execute(total);


        if (paymentResult.contains("success") || paymentResult.equals("pending")) {
            Order order = new Order(request.customerId(), total, Order.OrderStatus.COMPLETED, paymentStrategy.getSupportedPaymentMethod());
            Order saved = orderRepository.save(order);

            eventPublisher.publishEvent(new OrderSuccessDto(request.customerEmail(), saved.getId()));
            return "Order place successfully.";
        }

        return "Payment failed";
    }

    public List<String> findHistory(Integer customerId)
    {
        return orderRepository.findByCustomerId(customerId).stream()
                .map(order -> "Order #" + order.getId() + " - $" + order.getTotal() + " - " + order.getStatus())
                .toList();
    }

}
