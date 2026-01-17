package be.kicksync_backend.feature.order.service;

import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import be.kicksync_backend.feature.order.entity.OrderStatus;
import be.kicksync_backend.feature.order.dto.OrderCreateRequestDto;
import be.kicksync_backend.feature.order.dto.OrderResponseDto;
import be.kicksync_backend.feature.order.entity.Order;
import be.kicksync_backend.feature.order.entity.OrderItem;
import be.kicksync_backend.feature.order.repository.OrderRepository;
import be.kicksync_backend.feature.product.entity.Product;
import be.kicksync_backend.feature.product.repository.ProductRepository;
import be.kicksync_backend.feature.user.entity.User;
import be.kicksync_backend.feature.user.repository.UserRepository;
import be.kicksync_backend.common.annotation.DistributedLock;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public List<OrderResponseDto> createOrder(OrderCreateRequestDto requestDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 통합 결제 번호 생성 (merchantUid)
        String merchantUid = "ORD-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "-" + UUID.randomUUID().toString().substring(0, 8);

        // PartnerId 별로 OrderItem 그룹화
        Map<Long, List<OrderItem>> orderItemsByPartner = requestDto.getOrderItems().stream()
                .map(itemDto -> {
                    Product product = productRepository.findByIdForce(itemDto.getProductId())
                            .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

                    product.decreaseStock(itemDto.getQuantity());

                    log.info("[CONCURRENCY_TEST] 재고 차감: User={}, ProductId={}, 남은 재고={}, 요청 수량={}",
                            userId, product.getId(), product.getStock(), itemDto.getQuantity());

                    return OrderItem.builder()
                            .product(product)
                            .quantity(itemDto.getQuantity())
                            .orderPrice(product.getRetailPrice())
                            .build();
                })
                .collect(Collectors.groupingBy(item -> item.getProduct().getPartner().getId()));

        List<Order> savedOrders = new ArrayList<>();

        // 그룹별로 주문 생성
        for (Map.Entry<Long, List<OrderItem>> entry : orderItemsByPartner.entrySet()) {
            Long partnerId = entry.getKey();
            List<OrderItem> items = entry.getValue();
            if (items.isEmpty()) continue;

            Order order = Order.builder()
                    .user(user)
                    .receiverName(requestDto.getReceiverName())
                    .receiverPhone(requestDto.getReceiverPhone())
                    .address(new be.kicksync_backend.feature.order.entity.Address(
                            requestDto.getAddress().getZipcode(),
                            requestDto.getAddress().getStreet(),
                            requestDto.getAddress().getDetail()))
                    .requestMessage(requestDto.getRequestMessage())
                    .orderItems(items)
                    .partnerId(partnerId)
                    .merchantUid(merchantUid)
                    .build();

            for (OrderItem item : items) {
                item.setOrder(order);
            }

            savedOrders.add(orderRepository.save(order));
        }

        log.info("통합 주문 생성 완료: merchantUid={}, userId={}, splitOrders={}",
                merchantUid, userId, savedOrders.size());

        return savedOrders.stream()
                .map(OrderResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Order getOrderDetails(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
        if (!Objects.equals(order.getUser().getId(), userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACCESS);
        }
        return order;
    }

    @Transactional(readOnly = true)
    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findAllByUser_IdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public List<Long> startCancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (!userId.equals(order.getUser().getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACCESS);
        }

        order.markAsCancelling();

        return order.getOrderItems().stream()
                .map(item -> item.getProduct().getId())
                .toList();
    }

    @Transactional
    public void revertCancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
        order.revertCancelling();
        log.warn("주문 취소 롤백 (외부 결제 취소 실패): orderId={}, userId={}", orderId, userId);
    }

    @DistributedLock(
            key = "#productIds",
            waitTime = 10,
            leaseTime = 10,
            timeUnit = TimeUnit.SECONDS
    )
    @Transactional
    public void finalizeCancelOrder(Long orderId, Long userId, List<Long> productIds) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        order.cancel();

        for (OrderItem item : order.getOrderItems()) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
            product.increaseStock(item.getQuantity());
        }

        log.info("주문 취소 완료 (DB 업데이트 & 재고 복구): orderId={}, userId={}", orderId, userId);
    }

    @Transactional(readOnly = true)
    public boolean needsPaymentCancellation(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
        return order.getStatus() == OrderStatus.PAYMENT_COMPLETED ||
                order.getStatus() == OrderStatus.PREPARING;
    }
}
