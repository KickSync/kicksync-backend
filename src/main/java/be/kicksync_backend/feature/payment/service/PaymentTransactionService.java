package be.kicksync_backend.feature.payment.service;

import be.kicksync_backend.feature.order.entity.Order;
import be.kicksync_backend.feature.payment.domain.type.PaymentStatus;
import be.kicksync_backend.feature.payment.entity.Payment;
import be.kicksync_backend.feature.payment.repository.PaymentRepository;
import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentTransactionService {

    private final PaymentRepository paymentRepository;

    @Transactional
    public Payment savePaymentRecord(com.siot.IamportRestClient.response.Payment paymentInfo, Order order) {
        Optional<Payment> existingPayment = paymentRepository.findByImpUid(paymentInfo.getImpUid());
        if (existingPayment.isPresent()) {
            log.warn("이미 처리된 결제 검증입니다. impUid: {}", paymentInfo.getImpUid());
            return existingPayment.get();
        }

        String lastFourDigits = getLastFourDigits(paymentInfo);

        Payment payment = Payment.builder()
                .partnerId(order.getProduct().getPartnerId())
                .userId(order.getUser().getId())
                .orderId(order.getId())
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
                .build();
        try {
            return paymentRepository.save(payment);
        } catch (DataIntegrityViolationException e) {
            log.warn("결제 정보 저장 중 중복 발생 (impUid: {}), 기존 결제 정보를 조회합니다.", paymentInfo.getImpUid());
            return paymentRepository.findByImpUid(paymentInfo.getImpUid())
                    .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND_AFTER_DUPLICATION));
        }
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
