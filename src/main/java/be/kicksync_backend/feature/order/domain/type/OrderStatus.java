package be.kicksync_backend.feature.order.domain.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    PENDING_PAYMENT("결제 대기중"),
    PAYMENT_COMPLETED("결제 완료"),
    PREPARING("상품 준비중"),
    SHIPPED("배송중"),
    DELIVERED("배송 완료"),
    CANCELLED("주문 취소"),
    PAYMENT_FAILED("결제 실패");

    private final String description;
} 