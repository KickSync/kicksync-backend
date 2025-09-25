package be.kicksync_backend.feature.order.dto;

import be.kicksync_backend.feature.order.entity.Order;
import be.kicksync_backend.feature.product.entity.Product;
import be.kicksync_backend.feature.user.entity.User;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequestDto {
    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;
    @NotNull(message = "상품 ID는 필수입니다")
    private Long productId;

    public Order toEntity(User user, Product product, BigDecimal calculatedFinalPrice, LocalDateTime orderDate) {
        return Order.builder()
                .finalPrice(calculatedFinalPrice)
                .orderDate(orderDate)
                .user(user)
                .product(product)
                .build();
    }
}
