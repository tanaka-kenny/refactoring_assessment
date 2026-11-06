package dev.tanaka.refactoring_exercise;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer customerId;
    private BigDecimal total;
    private OrderStatus status;
    private PaymentMethod paymentMethod;

    public Order(
        Integer customerId, BigDecimal total, OrderStatus status, PaymentMethod paymentMethod) {
        this.customerId = customerId;
        this.total = total;
        this.status = status;
        this.paymentMethod = paymentMethod;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public enum OrderStatus {
        COMPLETED,
        FAILED
    }

    public enum PaymentMethod {
        CREDIT_CARD,
        PAY_PAL,
        BANK_TRANSFER

    }
}
