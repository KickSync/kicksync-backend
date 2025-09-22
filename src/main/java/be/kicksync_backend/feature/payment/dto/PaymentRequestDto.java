package be.kicksync_backend.feature.payment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentRequestDto {
    private String impUid;
    private String merchantUid;
    private Long orderId;
} 