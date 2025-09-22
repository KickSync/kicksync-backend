package be.kicksync_backend.feature.payment.service;

import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import be.kicksync_backend.feature.order.entity.Order;
import be.kicksync_backend.feature.order.repository.OrderRepository;
import be.kicksync_backend.feature.payment.util.PaymentClient;
import be.kicksync_backend.feature.payment.dto.PaymentCancelRequestDto;
import be.kicksync_backend.feature.payment.dto.PaymentRequestDto;
import be.kicksync_backend.feature.payment.entity.Payment;
import be.kicksync_backend.feature.payment.repository.PaymentRepository;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentClient paymentClient;
    private final PaymentTransactionService paymentTransactionService;

    public Payment verifyPayment(PaymentRequestDto requestDto) throws IamportResponseException, IOException {
        IamportResponse<com.siot.IamportRestClient.response.Payment> iamportResponse = paymentClient.getPaymentInfoByImpUid(requestDto.getImpUid());
        com.siot.IamportRestClient.response.Payment paymentInfo = iamportResponse.getResponse();

        Order order = orderRepository.findById(requestDto.getOrderId())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (paymentInfo.getAmount().compareTo(order.getFinalPrice()) != 0) {
            throw new CustomException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        return paymentTransactionService.savePaymentRecord(paymentInfo, order);
    }

    public Payment cancelPayment(PaymentCancelRequestDto cancelDto, Long userId) throws IamportResponseException, IOException {
        Payment payment = paymentRepository.findByImpUid(cancelDto.getImpUid())
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        if (!Objects.equals(payment.getUserId(), userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACTION);
        }

        IamportResponse<com.siot.IamportRestClient.response.Payment> iamportResponse = paymentClient.cancelPaymentByImpUid(cancelDto);

        return paymentTransactionService.updatePaymentStatusToCancelled(iamportResponse.getResponse());
    }

    @Transactional(readOnly = true)
    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<Payment> getMyPayments(Long userId) {
        return paymentRepository.findAllByUserId(userId);
    }
}
