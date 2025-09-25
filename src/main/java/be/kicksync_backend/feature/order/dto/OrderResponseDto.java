package be.kicksync_backend.feature.order.dto;

import be.kicksync_backend.feature.order.domain.type.OrderStatus;
import be.kicksync_backend.feature.order.entity.Order;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class OrderResponseDto {
    private final Long orderId;
    private final BigDecimal finalPrice;
    private final LocalDateTime orderDate;
    private final OrderStatus status;
    private final List<OrderItemResponseDto > orderItems;
    private final String merchantUid;  // 결제 요청을 위한 주문 고유 식별자

    public OrderResponseDto(Order order) {
        this.orderId = order.getId();
        this.finalPrice = order.getFinalPrice();
        this.orderDate = order.getOrderDate();
        this.status = order.getStatus();
        this.orderItems = order.getOrderItems().stream()
                .map(OrderItemResponseDto::new)
                .collect(Collectors.toList());
        this.merchantUid = order.getId().toString();
    }

    public static OrderResponseDto from(Order order) {
        return new OrderResponseDto(order);
    }
}