package be.kicksync_backend.feature.order.dto;

import be.kicksync_backend.feature.order.entity.OrderStatus;
import be.kicksync_backend.feature.order.entity.Order;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class OrderResponseDto {
    @Schema(description = "주문 ID", example = "1")
    private final Long orderId;
    
    @Schema(description = "최종 결제 금액", example = "150000")
    private final BigDecimal finalPrice;
    
    @Schema(description = "주문 일시")
    private final LocalDateTime orderDate;
    
    @Schema(description = "주문 상태", example = "COMPLETED")
    private final OrderStatus status;
    
    @Schema(description = "주문 상품 목록")
    private final List<OrderItemResponseDto > orderItems;
    
    @Schema(description = "주문 번호 (결제 연동용)", example = "1")
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