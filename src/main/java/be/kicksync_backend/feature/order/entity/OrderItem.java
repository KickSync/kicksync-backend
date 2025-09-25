package be.kicksync_backend.feature.order.entity;

import be.kicksync_backend.common.entity.BaseTimeEntity;
import be.kicksync_backend.feature.product.entity.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "order_items")
public class OrderItem extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal orderPrice;

    @Builder
    public OrderItem(Order order, Product product, Integer quantity, BigDecimal orderPrice) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.orderPrice = orderPrice;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}
