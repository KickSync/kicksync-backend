package be.kicksync_backend.feature.payment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentCancelRequestDto {
    private String impUid;
    private String reason;
}
