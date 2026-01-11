package be.kicksync_backend.feature.order.service;

import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import be.kicksync_backend.feature.order.dto.OrderCancelRequestDto;
import be.kicksync_backend.feature.order.dto.OrderCreateRequestDto;
import be.kicksync_backend.feature.order.dto.OrderItemRequestDto;
import be.kicksync_backend.feature.order.dto.OrderResponseDto;
import be.kicksync_backend.feature.payment.service.PaymentService;
import com.siot.IamportRestClient.exception.IamportResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderFacade {
    private final RedissonClient redissonClient;
    private final OrderService orderService;
    private final PaymentService paymentService;

    public OrderResponseDto createOrderWithLock(OrderCreateRequestDto requestDto, Long userId) {
        orderService.preValidateOrder(requestDto, userId);

        List<Long> productIds = requestDto.getOrderItems().stream()
                .map(OrderItemRequestDto::getProductId)
                .distinct()
                .sorted()
                .toList();

        List<RLock> locks = productIds.stream()
                .map(id -> redissonClient.getLock("lock:product:" + id))
                .toList();

        RLock multiLock = redissonClient.getMultiLock(locks.toArray(new RLock[0]));

        boolean isLocked = false;
        try {
            // waitTime: 10s, leaseTime: 5s (트랜잭션이 5초 이상 걸리면 락 자동 해제)
            isLocked = multiLock.tryLock(10, 5, TimeUnit.SECONDS);
            if (!isLocked) {
                log.warn("다중 락 획득 실패: user ID={}, items={}", userId, productIds);
                throw new CustomException(ErrorCode.LOCK_ACQUISITION_FAILED);
            }

            log.info("다중 락 획득 성공: user ID={}", userId);
            return orderService.createOrder(requestDto, userId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        } finally {
            if (isLocked) {
                try {
                    multiLock.unlock();
                    log.info("다중 락 해제: user ID={}", userId);
                } catch (IllegalMonitorStateException e) {
                    log.warn("이미 해제된 락입니다: user ID={}", userId);
                }
            }
        }
    }

    public void cancelOrder(Long orderId, OrderCancelRequestDto cancelDto, Long userId) {
        orderService.startCancelOrder(orderId, userId);

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
            orderService.finalizeCancelOrder(orderId, userId);
        } catch (Exception e) {
            log.error("주문 취소 최종 확정 실패 (심각한 불일치 위험): orderId={}, error={}", orderId, e.getMessage());
            throw e;
        }
    }
}