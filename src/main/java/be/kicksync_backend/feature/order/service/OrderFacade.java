package be.kicksync_backend.feature.order.service;

import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import be.kicksync_backend.feature.order.dto.OrderCreateRequestDto;
import be.kicksync_backend.feature.order.dto.OrderItemRequestDto;
import be.kicksync_backend.feature.order.dto.OrderResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderFacade {
    private final RedissonClient redissonClient;
    private final OrderService orderService;

    public OrderResponseDto createOrderWithLock(OrderCreateRequestDto requestDto, Long userId) {
        orderService.preValidateOrder(requestDto, userId);

        List<RLock> locks = requestDto.getOrderItems().stream()
                .map(item -> redissonClient.getLock("lock:product:" + item.getProductId()))
                .toList();
        RLock multiLock = redissonClient.getMultiLock(locks.toArray(new RLock[0]));

        boolean isLocked = false;
        try {
            isLocked = multiLock.tryLock(10, TimeUnit.SECONDS);
            if (!isLocked) {
                log.warn("다중 락 획득 실패: user ID={}, items={}", userId, requestDto.getOrderItems().stream()
                        .map(OrderItemRequestDto::getProductId).collect(Collectors.toList()));
                throw new CustomException(ErrorCode.LOCK_ACQUISITION_FAILED);
            }

            log.info("다중 락 획득 성공: user ID={}", userId);
            return orderService.createOrder(requestDto, userId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        } finally {
            if (isLocked && multiLock.isHeldByCurrentThread()) {
                multiLock.unlock();
                log.info("다중 락 해제: user ID={}", userId);
            }
        }
    }
}