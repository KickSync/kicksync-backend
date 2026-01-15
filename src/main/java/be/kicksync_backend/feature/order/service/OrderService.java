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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public void preValidateOrder(OrderCreateRequestDto requestDto, Long userId) {
        if (requestDto.getOrderItems() == null || requestDto.getOrderItems().isEmpty()) {
            throw new CustomException(ErrorCode.EMPTY_ORDER_ITEMS);
        }

        userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Long> partnerIds = requestDto.getOrderItems().stream()
                .map(itemDto -> {
                    Product product = productRepository.findById(itemDto.getProductId())
                            .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
                    return product.getPartnerId();
                })
                .distinct()
                .toList();

        if (partnerIds.size() > 1) {
            throw new CustomException(ErrorCode.MULTIPLE_PARTNERS_IN_ORDER);
        }
    }

    public OrderResponseDto createOrder(OrderCreateRequestDto requestDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<OrderItem> orderItems = requestDto.getOrderItems().stream()
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
                }).toList();

        BigDecimal totalAmount = orderItems.stream()
                .map(item -> item.getOrderPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .user(user)
                .receiverName(requestDto.getReceiverName())
                .receiverPhone(requestDto.getReceiverPhone())
                .address(new be.kicksync_backend.feature.order.entity.Address(
                        requestDto.getAddress().getZipcode(),
                        requestDto.getAddress().getStreet(),
                        requestDto.getAddress().getDetail()))
                .requestMessage(requestDto.getRequestMessage())
                .orderItems(orderItems)
                .build();

        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }

        Order savedOrder = orderRepository.save(order);

        log.info("주문 생성 완료: orderId={}, userId={}, items={}",
                savedOrder.getId(), userId, savedOrder.getOrderItems().size());

        return OrderResponseDto.from(savedOrder);
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
    public void startCancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (!userId.equals(order.getUser().getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACCESS);
        }

        order.markAsCancelling();
    }

    @Transactional
    public void revertCancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
        order.revertCancelling();
        log.warn("주문 취소 롤백 (외부 결제 취소 실패): orderId={}, userId={}", orderId, userId);
    }

    @Transactional
    public void finalizeCancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        order.cancel();

        for (OrderItem item : order.getOrderItems()) {
            productRepository.increaseStock(item.getProduct().getId(), item.getQuantity());
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