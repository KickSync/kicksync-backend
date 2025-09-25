package be.kicksync_backend.feature.order.dto;

import be.kicksync_backend.feature.order.entity.OrderItem;
import be.kicksync_backend.feature.product.dto.SimpleProductResponseDto;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class OrderItemResponseDto {
    private final Long orderItemId;
    private final SimpleProductResponseDto product;
    private final Integer quantity;
    private final BigDecimal orderPrice;

    public OrderItemResponseDto(OrderItem orderItem) {
        this.orderItemId = orderItem.getId();
        this.product = new SimpleProductResponseDto(orderItem.getProduct());
        this.quantity = orderItem.getQuantity();
        this.orderPrice = orderItem.getOrderPrice();
    }
}