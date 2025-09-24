package be.kicksync_backend.feature.order.dto;

import be.kicksync_backend.feature.order.domain.type.OrderStatus;
import be.kicksync_backend.feature.order.entity.Order;
import be.kicksync_backend.feature.product.dto.SimpleProductResponseDto;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class OrderResponseDto {
    private final Long orderId;
    private final BigDecimal finalPrice;
    private final LocalDateTime orderDate;
    private final OrderStatus status;
    private final SimpleProductResponseDto product;

    public OrderResponseDto(Order order) {
        this.orderId = order.getId();
        this.finalPrice = order.getFinalPrice();
        this.orderDate = order.getOrderDate();
        this.status = order.getStatus();
        this.product = new SimpleProductResponseDto(order.getProduct());
    }
} 