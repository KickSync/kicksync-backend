package be.kicksync_backend.feature.payment.service;

import be.kicksync_backend.feature.order.entity.Order;
import be.kicksync_backend.feature.payment.domain.type.PaymentStatus;
import be.kicksync_backend.feature.payment.entity.Payment;
import be.kicksync_backend.feature.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        if (paymentRepository.findByImpUid(paymentInfo.getImpUid()).isPresent()) {
            log.warn("이미 처리된 결제 검증입니다. impUid: {}", paymentInfo.getImpUid());
            return paymentRepository.findByImpUid(paymentInfo.getImpUid()).get();
        }

        String fullCardNumber = paymentInfo.getCardNumber();
        String lastFourDigits = null;
        if (fullCardNumber != null && !fullCardNumber.isBlank() && fullCardNumber.length() > 4) {
            lastFourDigits = fullCardNumber.substring(fullCardNumber.length() - 4);
        } else if (fullCardNumber != null && !fullCardNumber.isBlank()) {
            lastFourDigits = fullCardNumber;
        }

        Payment payment = Payment.builder()
                .partnerId(order.getProduct().getPartnerId())
                .userId(order.getUser().getId())
                .orderId(order.getId())
                .paymentAmount(paymentInfo.getAmount())
                .paymentDate(LocalDateTime.ofInstant(Instant.ofEpochSecond(paymentInfo.getPaidAt().getTime() / 1000), ZoneId.systemDefault()))
                .impUid(paymentInfo.getImpUid())
                .paymentMethod(paymentInfo.getPayMethod())
                .merchantUid(paymentInfo.getMerchantUid())
                .pgProvider(paymentInfo.getPgProvider())
                .pgTid(paymentInfo.getPgTid())
                .status(PaymentStatus.fromValue(paymentInfo.getStatus()))
                .cardName(paymentInfo.getCardName())
                .cardNumber(lastFourDigits)
                .build();
        return paymentRepository.save(payment);
    }
}
