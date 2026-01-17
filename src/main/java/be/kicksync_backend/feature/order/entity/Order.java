package be.kicksync_backend.feature.order.entity;

import be.kicksync_backend.common.entity.BaseTimeEntity;
import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import be.kicksync_backend.feature.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "orders")
public class Order extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal finalPrice;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Embedded
    @Column(nullable = false)
    private Address address;

    @Column(nullable = false)
    private String receiverName;

    @Column(nullable = false)
    private String receiverPhone;

    private String requestMessage;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(nullable = false)
    private Long partnerId;

    @Column(nullable = false)
    private String merchantUid;

    @Builder
    public Order(User user, Address address, String receiverName, String receiverPhone, String requestMessage, List<OrderItem> orderItems, Long partnerId, String merchantUid) {
        this.user = user;
        this.address = address;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.requestMessage = requestMessage;
        this.orderDate = LocalDateTime.now();
        this.status = OrderStatus.PENDING_PAYMENT;
        this.partnerId = partnerId;
        this.merchantUid = merchantUid;
        orderItems.forEach(this::addOrderItem);
        this.finalPrice = calculateTotalPrice();
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public BigDecimal calculateTotalPrice() {
        return orderItems.stream()
                .map(orderItem -> orderItem.getOrderPrice().multiply(new BigDecimal(orderItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void cancel() {
        if (status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED) {
            throw new CustomException(ErrorCode.ORDER_CANCEL_NOT_ALLOWED_SHIPPED);
        }
        if (status == OrderStatus.CANCELLED) {
            throw new CustomException(ErrorCode.ORDER_ALREADY_CANCELLED);
        }
        this.status = OrderStatus.CANCELLED;
    }

    public void markAsCancelling() {
        if (status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED) {
            throw new CustomException(ErrorCode.ORDER_CANCEL_NOT_ALLOWED_SHIPPED);
        }
        if (status == OrderStatus.CANCELLED) {
            throw new CustomException(ErrorCode.ORDER_ALREADY_CANCELLED);
        }
        this.status = OrderStatus.CANCELLING;
    }

    public void revertCancelling() {
        if (this.status != OrderStatus.CANCELLING) {
            throw new CustomException(ErrorCode.INVALID_ORDER_STATE);
        }
        this.status = OrderStatus.PREPARING;
    }

    public void processPaymentSuccess() {
        if (this.status != OrderStatus.PENDING_PAYMENT) {
            throw new CustomException(ErrorCode.INVALID_ORDER_STATE);
        }
        this.status = OrderStatus.PREPARING;
    }

    public void failPayment() {
        if (this.status != OrderStatus.PENDING_PAYMENT) {
            throw new CustomException(ErrorCode.INVALID_ORDER_STATE);
        }
        this.status = OrderStatus.PAYMENT_FAILED;
    }

    public void ship() {
        if (this.status != OrderStatus.PREPARING) {
            throw new CustomException(ErrorCode.ORDER_SHIP_NOT_ALLOWED_NOT_PREPARING);
        }
        this.status = OrderStatus.SHIPPED;
    }

    public void deliver() {
        if (this.status != OrderStatus.SHIPPED) {
            throw new CustomException(ErrorCode.ORDER_DELIVER_NOT_ALLOWED_NOT_SHIPPED);
        }
        this.status = OrderStatus.DELIVERED;
    }

    public void settle() {
        if (this.status != OrderStatus.DELIVERED) {
            throw new CustomException(ErrorCode.ORDER_SETTLE_NOT_ALLOWED_NOT_DELIVERED);
        }
        this.status = OrderStatus.SETTLED;
    }
} 