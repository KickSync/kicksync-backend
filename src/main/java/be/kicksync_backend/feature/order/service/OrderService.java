package be.kicksync_backend.feature.order.service;

import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import be.kicksync_backend.feature.order.entity.OrderStatus;
import be.kicksync_backend.feature.order.dto.OrderCancelRequestDto;
import be.kicksync_backend.feature.order.dto.OrderCreateRequestDto;
import be.kicksync_backend.feature.order.dto.OrderResponseDto;
import be.kicksync_backend.feature.order.entity.Order;
import be.kicksync_backend.feature.order.entity.OrderItem;
import be.kicksync_backend.feature.order.repository.OrderRepository;
import be.kicksync_backend.feature.payment.service.PaymentService;
import be.kicksync_backend.feature.product.entity.Product;
import be.kicksync_backend.feature.product.repository.ProductRepository;
import be.kicksync_backend.feature.user.entity.User;
import be.kicksync_backend.feature.user.repository.UserRepository;
import com.siot.IamportRestClient.exception.IamportResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PaymentService paymentService;

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
                .collect(Collectors.toList());

        if (partnerIds.size() > 1) {
            throw new CustomException(ErrorCode.MULTIPLE_PARTNERS_IN_ORDER);
        }
    }

    public OrderResponseDto createOrder(OrderCreateRequestDto requestDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<OrderItem> orderItems = requestDto.getOrderItems().stream()
                .map(itemDto -> {
                    Product product = productRepository.findById(itemDto.getProductId())
                            .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

                    product.decreaseStock(itemDto.getQuantity());

                    return OrderItem.builder()
                            .product(product)
                            .quantity(itemDto.getQuantity())
                            .orderPrice(product.getRetailPrice())
                            .build();
                }).collect(Collectors.toList());

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

        Order savedOrder = orderRepository.save(order);

        log.info("주문 생성 완료: orderId={}, userId={}, items={}",
                savedOrder.getId(), userId, savedOrder.getOrderItems().size());

        return OrderResponseDto.from(savedOrder);
    }

    public void completePayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        order.processPaymentSuccess();

        log.info("주문 결제 완료: orderId={}", orderId);
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

    public void cancelOrder(Long orderId, OrderCancelRequestDto cancelDto, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (!userId.equals(order.getUser().getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACCESS);
        }

        String reason = (cancelDto != null) ? cancelDto.getReason() : "사용자 요청에 의한 취소";

        if (order.getStatus() == OrderStatus.PAYMENT_COMPLETED ||
                order.getStatus() == OrderStatus.PREPARING) {
            try {
                paymentService.cancelPaymentForOrder(orderId, reason);
            } catch (IamportResponseException | IOException e) {
                log.error("Iamport 결제 취소 중 오류 발생: orderId={}, error={}", orderId, e.getMessage());
                throw new CustomException(ErrorCode.PAYMENT_CANCEL_FAILED);
            }
        }

        order.cancel();

        log.info("주문 취소 완료: orderId={}, userId={}, reason={}", orderId, userId, reason);
    }
}