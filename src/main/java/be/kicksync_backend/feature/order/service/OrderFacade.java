package be.kicksync_backend.feature.order.service;

import be.kicksync_backend.common.annotation.DistributedLock;
import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import be.kicksync_backend.feature.order.dto.OrderCancelRequestDto;
import be.kicksync_backend.feature.order.dto.OrderCreateRequestDto;
import be.kicksync_backend.feature.order.dto.OrderResponseDto;
import be.kicksync_backend.feature.payment.service.PaymentService;
import com.siot.IamportRestClient.exception.IamportResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderFacade {
    private final OrderService orderService;
    private final PaymentService paymentService;

    @DistributedLock(
            key = "#requestDto.orderItems.![productId]",
            waitTime = 10,
            leaseTime = 10,
            timeUnit = TimeUnit.SECONDS
    )
    public OrderResponseDto createOrderWithLock(OrderCreateRequestDto requestDto, Long userId) {
        return orderService.createOrder(requestDto, userId);
    }

    public void cancelOrder(Long orderId, OrderCancelRequestDto cancelDto, Long userId) {
        List<Long> productIds = orderService.startCancelOrder(orderId, userId);

        String reason = (cancelDto != null) ? cancelDto.getReason() : "사용자 요청에 의한 취소";

        if (orderService.needsPaymentCancellation(orderId)) {
            try {
                paymentService.cancelPaymentForOrder(orderId, reason);
            } catch (Exception e) {
                log.error("Iamport 결제 취소 실패, 상태 롤백: orderId={}, error={}", orderId, e.getMessage());
                orderService.revertCancelOrder(orderId, userId);
                if (e instanceof IamportResponseException || e instanceof IOException) {
                    throw new CustomException(ErrorCode.PAYMENT_CANCEL_FAILED);
                }
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
        }

        try {
            orderService.finalizeCancelOrder(orderId, userId, productIds);
        } catch (Exception e) {
            log.error("주문 취소 최종 확정 실패 (심각한 불일치 위험): orderId={}, error={}", orderId, e.getMessage());
            throw e;
        }
    }
}