package be.kicksync_backend.feature.order.dto;

import be.kicksync_backend.feature.order.entity.OrderItem;
import be.kicksync_backend.feature.product.dto.SimpleProductResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class OrderItemResponseDto {
    @Schema(description = "주문 상품 ID", example = "1")
    private final Long orderItemId;
    
    @Schema(description = "상품 정보")
    private final SimpleProductResponseDto product;
    
    @Schema(description = "주문 수량", example = "1")
    private final Integer quantity;
    
    @Schema(description = "주문 가격", example = "150000")
    private final BigDecimal orderPrice;

    public OrderItemResponseDto(OrderItem orderItem) {
        this.orderItemId = orderItem.getId();
        this.product = new SimpleProductResponseDto(orderItem.getProduct());
        this.quantity = orderItem.getQuantity();
        this.orderPrice = orderItem.getOrderPrice();
    }
}