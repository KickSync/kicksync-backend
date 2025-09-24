package be.kicksync_backend.feature.order.dto;

import be.kicksync_backend.feature.order.entity.Order;
import be.kicksync_backend.feature.product.entity.Product;
import be.kicksync_backend.feature.user.entity.User;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequestDto {
    @NotNull(message = "상품 ID는 필수입니다")
    private Long productId;

    public Order toEntity(User user, Product product) {
        return Order.builder()
                .finalPrice(product.getRetailPrice())
                .orderDate(LocalDateTime.now())
                .user(user)
                .product(product)
                .build();
    }
}
