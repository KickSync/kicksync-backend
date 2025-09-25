package be.kicksync_backend.feature.payment.service;

import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import be.kicksync_backend.feature.order.entity.Order;
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
                .status(paymentInfo.getStatus())
                .cardName(paymentInfo.getCardName())
                .cardNumber(paymentInfo.getCardNumber())
                .build();
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment updatePaymentStatusToCancelled(com.siot.IamportRestClient.response.Payment paymentInfo) {
        Payment payment = paymentRepository.findByImpUid(paymentInfo.getImpUid())
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
        try {
            payment.changeStatus(paymentInfo.getStatus());
            return paymentRepository.save(payment);
        } catch (Exception e) {
            log.error("[CRITICAL] PortOne 결제 취소는 성공했으나 DB 상태 업데이트 실패! 수동 확인 필요. imp_uid: {}", paymentInfo.getImpUid());
            throw new CustomException(ErrorCode.DATABASE_UPDATE_FAILED);
        }
    }
}
