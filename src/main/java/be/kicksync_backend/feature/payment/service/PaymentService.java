package be.kicksync_backend.feature.payment.service;

import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import be.kicksync_backend.feature.order.entity.Order;
import be.kicksync_backend.feature.order.repository.OrderRepository;
import be.kicksync_backend.feature.payment.dto.PaymentCancelRequestDto;
import be.kicksync_backend.feature.payment.dto.PaymentRequestDto;
import be.kicksync_backend.feature.payment.entity.Payment;
import be.kicksync_backend.feature.payment.repository.PaymentRepository;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private IamportClient iamportClient;

    @Value("${iamport.api.key}")
    private String apiKey;

    @Value("${iamport.api.secret}")
    private String apiSecret;

    @PostConstruct
    public void init() {
        this.iamportClient = new IamportClient(apiKey, apiSecret);
    }

    @Transactional
    public Payment verifyPayment(PaymentRequestDto requestDto) throws IamportResponseException, IOException {
        IamportResponse<com.siot.IamportRestClient.response.Payment> iamportResponse = iamportClient.paymentByImpUid(requestDto.getImpUid());
        com.siot.IamportRestClient.response.Payment paymentInfo = iamportResponse.getResponse();

        Order order = orderRepository.findById(requestDto.getOrderId())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (paymentInfo.getAmount().compareTo(order.getFinalPrice()) != 0) {
            throw new CustomException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        Payment payment = Payment.builder()
                .userId(order.getUser().getId())
                .orderId(order.getId())
                .paymentAmount(paymentInfo.getAmount())
                .paymentDate(LocalDateTime.ofInstant(Instant.ofEpochSecond(paymentInfo.getPaidAt().getTime() / 1000), ZoneId.systemDefault()))
                .impUid(paymentInfo.getImpUid())
                .paymentMethod(paymentInfo.getPayMethod())
                .merchantUid(paymentInfo.getMerchantUid())
                .pgProvider(paymentInfo.getPgProvider())
                .pgTid(paymentInfo.getPgTid())
                .status(paymentInfo.getStatus())
                .cardName(paymentInfo.getCardName())
                .cardNumber(paymentInfo.getCardNumber())
                .build();
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment cancelPayment(PaymentCancelRequestDto cancelDto, Long userId) throws IamportResponseException, IOException {
        Payment payment = paymentRepository.findByImpUid(cancelDto.getImpUid())
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        if (!Objects.equals(payment.getUserId(), userId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACTION);
        }

        CancelData cancelData = new CancelData(cancelDto.getImpUid(), true); // true: 전액 환불
        cancelData.setReason(cancelDto.getReason());

        IamportResponse<com.siot.IamportRestClient.response.Payment> iamportResponse = iamportClient.cancelPaymentByImpUid(cancelData);

        payment.changeStatus(iamportResponse.getResponse().getStatus());
        return paymentRepository.save(payment);
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