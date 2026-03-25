package be.kicksync_backend.feature.payment.dto;

import be.kicksync_backend.feature.payment.entity.Payment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PaymentResponseDto {
    @Schema(description = "결제 ID", example = "1")
    private final Long paymentId;
    
    @Schema(description = "주문 번호 (Unified)", example = "ORD-20240101-UUID")
    private final String merchantUid;
    
    @Schema(description = "결제 금액", example = "150000")
    private final BigDecimal paymentAmount;
    
    @Schema(description = "결제 일시")
    private final LocalDateTime paymentDate;
    
    @Schema(description = "결제 수단", example = "card")
    private final String paymentMethod;
    
    @Schema(description = "결제 상태", example = "PAID")
    private final String status;
    
    @Schema(description = "카드사", example = "신한카드")
    private final String cardName;

    public static PaymentResponseDto from(Payment payment) {
        return PaymentResponseDto.builder()
                .paymentId(payment.getId())
                .merchantUid(payment.getMerchantUid())
                .paymentAmount(payment.getPaymentAmount())
                .paymentDate(payment.getPaymentDate())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus() != null ? payment.getStatus().getValue() : null)
                .cardName(payment.getCardName())
                .build();
    }

    public static List<PaymentResponseDto> from(List<Payment> payments) {
        return payments.stream()
                .map(PaymentResponseDto::from)
                .toList();
    }
}
