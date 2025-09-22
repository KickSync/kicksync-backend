package be.kicksync_backend.feature.payment.dto;

import be.kicksync_backend.feature.payment.entity.Payment;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class PaymentResponseDto {
    private final Long paymentId;
    private final Long orderId;
    private final BigDecimal paymentAmount;
    private final LocalDateTime paymentDate;
    private final String paymentMethod;
    private final String status;
    private final String cardName;

    public static PaymentResponseDto from(Payment payment) {
        return PaymentResponseDto.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .paymentAmount(payment.getPaymentAmount())
                .paymentDate(payment.getPaymentDate())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .cardName(payment.getCardName())
                .build();
    }

    public static List<PaymentResponseDto> from(List<Payment> payments) {
        return payments.stream()
                .map(PaymentResponseDto::from)
                .collect(Collectors.toList());
    }
}
