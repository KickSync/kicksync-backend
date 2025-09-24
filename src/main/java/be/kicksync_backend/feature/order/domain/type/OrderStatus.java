package be.kicksync_backend.feature.order.domain.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    PREPARING("상품 준비중"),
    SHIPPED("배송중"),
    DELIVERED("배송 완료"),
    CANCELLED("주문 취소");

    private final String description;
} 