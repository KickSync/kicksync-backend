package be.kicksync_backend.feature.payment.service;

import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import be.kicksync_backend.feature.order.entity.OrderStatus;
import be.kicksync_backend.feature.order.entity.Order;
import be.kicksync_backend.feature.order.repository.OrderRepository;
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
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String PAID_STATUS = "paid";
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentClient paymentClient;
    private final PaymentTransactionService paymentTransactionService;

    public Payment verifyPayment(PaymentRequestDto requestDto, Long userId) throws IamportResponseException, IOException {
        Optional<Payment> existingPayment = paymentRepository.findByImpUid(requestDto.getImpUid());
        if (existingPayment.isPresent()) {
            Payment payment = existingPayment.get();
            if (payment.getStatus() == PaymentStatus.PAID) {
                log.info("이미 성공적으로 처리된 결제입니다. impUid={}", requestDto.getImpUid());
                return payment;
            }
            throw new CustomException(ErrorCode.PAYMENT_ALREADY_CANCELLED);
        }

        IamportResponse<com.siot.IamportRestClient.response.Payment> iamportResponse =
                paymentClient.getPaymentInfoByImpUid(requestDto.getImpUid());
        com.siot.IamportRestClient.response.Payment paymentInfo = iamportResponse.getResponse();

        if (paymentInfo == null) {
            throw new CustomException(ErrorCode.PAYMENT_VERIFICATION_FAILED);
        }

        Order order = orderRepository.findById(requestDto.getOrderId())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACCESS);
        }

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new CustomException(ErrorCode.INVALID_ORDER_STATE);
        }

        if (paymentInfo.getAmount().compareTo(order.getFinalPrice()) != 0) {
            throw new CustomException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        if (!order.getId().toString().equals(paymentInfo.getMerchantUid())) {
            throw new CustomException(ErrorCode.PAYMENT_MERCHANT_UID_MISMATCH);
        }

        if (!PAID_STATUS.equalsIgnoreCase(paymentInfo.getStatus())) {
            throw new CustomException(ErrorCode.PAYMENT_STATUS_NOT_PAID);
        }

        return paymentTransactionService.completePaymentVerification(paymentInfo, order);
    }

    public void cancelPaymentForOrder(Long orderId, String reason) throws IamportResponseException, IOException {
        Payment payment = getPaymentByOrderId(orderId);
        IamportResponse<com.siot.IamportRestClient.response.Payment> iamportResponse =
                paymentClient.cancelPaymentByImpUid(payment.getImpUid(), reason);

        if (iamportResponse.getResponse() == null) {
            throw new CustomException(ErrorCode.PAYMENT_CANCEL_FAILED);
        }

        paymentTransactionService.finalizePaymentCancellation(payment.getId(), reason);
    }

    private Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrder_Id(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Payment getPaymentByOrderId(Long orderId, Long userId) {
        return paymentRepository.findByOrder_IdAndUserId(orderId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<Payment> getMyPayments(Long userId) {
        return paymentRepository.findAllByUserId(userId);
    }
}
