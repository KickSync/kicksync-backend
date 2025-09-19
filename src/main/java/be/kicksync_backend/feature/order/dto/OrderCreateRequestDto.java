package be.kicksync_backend.feature.order.dto;

import be.kicksync_backend.feature.order.entity.Order;
import be.kicksync_backend.feature.product.entity.Product;
import be.kicksync_backend.feature.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class OrderCreateRequestDto {
    private BigDecimal finalPrice;
    private LocalDateTime orderDate;
    private Long userId;
    private Long productId;

    public Order toEntity(User user, Product product) {
        return Order.builder()
                .finalPrice(this.finalPrice)
                .orderDate(this.orderDate)
                .user(user)
                .product(product)
                .build();
    }
} 