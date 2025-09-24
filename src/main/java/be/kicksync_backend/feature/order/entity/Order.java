package be.kicksync_backend.feature.order.entity;

import be.kicksync_backend.common.entity.BaseTimeEntity;
import be.kicksync_backend.feature.order.domain.type.OrderStatus;
import be.kicksync_backend.feature.product.entity.Product;
import be.kicksync_backend.feature.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Builder
    public Order(BigDecimal finalPrice, LocalDateTime orderDate, User user, Product product) {
        this.finalPrice = finalPrice;
        this.orderDate = orderDate;
        this.status = OrderStatus.PREPARING;
        this.user = user;
        this.product = product;
    }

    public void cancel() {
        this.status = OrderStatus.CANCELLED;
    }
} 