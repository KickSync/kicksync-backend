package be.kicksync_backend.feature.order.service;

import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import be.kicksync_backend.feature.order.dto.OrderCancelRequestDto;
import be.kicksync_backend.feature.order.dto.OrderCreateRequestDto;
import be.kicksync_backend.feature.order.entity.Order;
import be.kicksync_backend.feature.order.repository.OrderRepository;
import be.kicksync_backend.feature.payment.service.PaymentService;
import be.kicksync_backend.feature.product.entity.Product;
import be.kicksync_backend.feature.product.repository.ProductRepository;
import be.kicksync_backend.feature.user.entity.User;
import be.kicksync_backend.feature.user.repository.UserRepository;
import com.siot.IamportRestClient.exception.IamportResponseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PaymentService paymentService;

    public Order createOrder(OrderCreateRequestDto requestDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Product product = productRepository.findByIdWithPessimisticLock(requestDto.getProductId())
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        product.decreaseStock(1);
        Order order = requestDto.toEntity(user, product);
        return orderRepository.save(order);
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

    public void cancelOrder(Long orderId, OrderCancelRequestDto cancelDto, Long userId) throws IamportResponseException, IOException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (!userId.equals(order.getUser().getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACCESS);
        }

        String reason = (cancelDto != null) ? cancelDto.getReason() : null;

        paymentService.cancelPaymentForOrder(orderId, reason);
        order.cancel();

        Product product = productRepository.findByIdWithPessimisticLock(order.getProduct().getId())
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        product.decreaseStock(-1);
    }
} 