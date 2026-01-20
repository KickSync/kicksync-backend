package be.kicksync_backend.feature.payment.service;

import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import be.kicksync_backend.feature.order.entity.OrderStatus;
import be.kicksync_backend.feature.order.entity.Order;
import be.kicksync_backend.feature.order.repository.OrderRepository;
import be.kicksync_backend.feature.payment.dto.PaymentResponseDto;
import be.kicksync_backend.feature.payment.util.PaymentClient;
import be.kicksync_backend.feature.payment.dto.PaymentRequestDto;
import be.kicksync_backend.feature.payment.entity.Payment;
import be.kicksync_backend.feature.payment.entity.PaymentStatus;
import be.kicksync_backend.feature.payment.repository.PaymentRepository;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String PAID_STATUS = "paid";
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentClient paymentClient;
    private final PaymentTransactionService paymentTransactionService;

    @Transactional
    public PaymentResponseDto verifyPayment(PaymentRequestDto requestDto, Long userId) throws IamportResponseException, IOException {
        String merchantUid = requestDto.getMerchantUid();
        
        Optional<Payment> existingPayment = paymentRepository.findByImpUid(requestDto.getImpUid());
        if (existingPayment.isPresent()) {
            Payment payment = existingPayment.get();
            if (payment.getStatus() == PaymentStatus.PAID) {
                log.info("이미 성공적으로 처리된 결제입니다. impUid={}", requestDto.getImpUid());
                return PaymentResponseDto.from(payment);
            }
            throw new CustomException(ErrorCode.PAYMENT_ALREADY_CANCELLED);
        }

        IamportResponse<com.siot.IamportRestClient.response.Payment> iamportResponse =
                paymentClient.getPaymentInfoByImpUid(requestDto.getImpUid());
        com.siot.IamportRestClient.response.Payment paymentInfo = iamportResponse.getResponse();

        if (paymentInfo == null) {
            throw new CustomException(ErrorCode.PAYMENT_VERIFICATION_FAILED);
        }

        // 통합 결제 검증: merchantUid로 모든 주문 조회
        if (merchantUid == null && requestDto.getOrderId() != null) {
             Order order = orderRepository.findById(requestDto.getOrderId())
                     .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
             merchantUid = order.getMerchantUid();
        }
        
        List<Order> orders = orderRepository.findAllByMerchantUid(merchantUid);
        if (orders.isEmpty()) {
            throw new CustomException(ErrorCode.ORDER_NOT_FOUND);
        }

        Order firstOrder = orders.get(0);
        if (!firstOrder.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACCESS);
        }

        BigDecimal totalOrderPrice = BigDecimal.ZERO;
        for (Order order : orders) {
            if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
                throw new CustomException(ErrorCode.INVALID_ORDER_STATE);
            }
            totalOrderPrice = totalOrderPrice.add(order.getFinalPrice());
        }

        if (paymentInfo.getAmount().compareTo(totalOrderPrice) != 0) {
            throw new CustomException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        if (!merchantUid.equals(paymentInfo.getMerchantUid())) {
            throw new CustomException(ErrorCode.PAYMENT_MERCHANT_UID_MISMATCH);
        }

        if (!PAID_STATUS.equalsIgnoreCase(paymentInfo.getStatus())) {
            throw new CustomException(ErrorCode.PAYMENT_STATUS_NOT_PAID);
        }

        Payment savedPayment = paymentTransactionService.completePaymentVerification(paymentInfo, orders);
        return PaymentResponseDto.from(savedPayment);
    }

    public void cancelPaymentForOrder(Long orderId, String reason) throws IamportResponseException, IOException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
        
        Payment payment = paymentRepository.findByMerchantUid(order.getMerchantUid())
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        // 부분 취소 (해당 주문 금액만큼)
        BigDecimal cancelAmount = order.getFinalPrice();
        
        IamportResponse<com.siot.IamportRestClient.response.Payment> iamportResponse =
                paymentClient.cancelPaymentByImpUid(payment.getImpUid(), cancelAmount, reason, null); 
        
        if (iamportResponse.getResponse() == null) {
            throw new CustomException(ErrorCode.PAYMENT_CANCEL_FAILED);
        }

        paymentTransactionService.finalizePaymentCancellation(payment.getId(), reason);
    }

    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentByOrderId(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
        if (!order.getUser().getId().equals(userId)) {
             throw new CustomException(ErrorCode.FORBIDDEN_ACCESS);
        }
        Payment payment = paymentRepository.findByMerchantUid(order.getMerchantUid())
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
        return PaymentResponseDto.from(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getMyPayments(Long userId) {
        return paymentRepository.findAllByUserId(userId).stream()
                .map(PaymentResponseDto::from)
                .collect(Collectors.toList());
    }
}