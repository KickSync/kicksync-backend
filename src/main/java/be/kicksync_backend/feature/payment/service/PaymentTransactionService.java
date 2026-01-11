package be.kicksync_backend.feature.payment.service;

import be.kicksync_backend.feature.order.repository.OrderRepository;
import be.kicksync_backend.feature.order.entity.Order;
import be.kicksync_backend.feature.payment.entity.PaymentStatus;
import be.kicksync_backend.feature.payment.entity.Payment;
import be.kicksync_backend.feature.payment.repository.PaymentRepository;
import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class PaymentTransactionService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public Payment savePaymentRecord(com.siot.IamportRestClient.response.Payment paymentInfo, Order order) {
        Optional<Payment> existingPayment = paymentRepository.findByImpUid(paymentInfo.getImpUid());
        if (existingPayment.isPresent()) {
            return existingPayment.get();
        }

        String lastFourDigits = getLastFourDigits(paymentInfo);

        if (order.getOrderItems().isEmpty()) {
            throw new CustomException(ErrorCode.ORDER_NOT_FOUND);
        }

        Payment payment = Payment.builder()
                .partnerId(order.getOrderItems().get(0).getProduct().getPartnerId())
                .user(order.getUser())
                .order(order)
                .paymentAmount(paymentInfo.getAmount())
                .paymentDate(LocalDateTime.ofInstant(Instant.ofEpochMilli(paymentInfo.getPaidAt().getTime()), ZoneId.systemDefault()))
                .impUid(paymentInfo.getImpUid())
                .paymentMethod(paymentInfo.getPayMethod())
                .merchantUid(paymentInfo.getMerchantUid())
                .pgProvider(paymentInfo.getPgProvider())
                .pgTid(paymentInfo.getPgTid())
                .status(PaymentStatus.fromValue(paymentInfo.getStatus()))
                .cardName(paymentInfo.getCardName())
                .cardNumber(lastFourDigits)
                .requestedAt(LocalDateTime.now())
                .approvedAt(PaymentStatus.fromValue(paymentInfo.getStatus()) == PaymentStatus.PAID ? LocalDateTime.now() : null)
                .build();

        try {
            return paymentRepository.save(payment);
        } catch (DataIntegrityViolationException e) {
            return paymentRepository.findByImpUid(paymentInfo.getImpUid())
                    .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND_AFTER_DUPLICATION));
        }
    }

    @Transactional
    public Payment completePaymentVerification(com.siot.IamportRestClient.response.Payment paymentInfo, Order order) {
        Payment payment = savePaymentRecord(paymentInfo, order);
        order.processPaymentSuccess();
        orderRepository.save(order);
        return payment;
    }

    @Transactional
    public void finalizePaymentCancellation(Long paymentId, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
        payment.updateOnCancel(reason);
    }

    private static String getLastFourDigits(com.siot.IamportRestClient.response.Payment paymentInfo) {
        String fullCardNumber = paymentInfo.getCardNumber();
        String lastFourDigits = null;
        if (fullCardNumber != null && !fullCardNumber.isBlank() && fullCardNumber.length() > 4) {
            lastFourDigits = fullCardNumber.substring(fullCardNumber.length() - 4);
        } else if (fullCardNumber != null && !fullCardNumber.isBlank()) {
            lastFourDigits = fullCardNumber;
        }
        return lastFourDigits;
    }
}
