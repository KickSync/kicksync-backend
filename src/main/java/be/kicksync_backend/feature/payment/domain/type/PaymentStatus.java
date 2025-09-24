package be.kicksync_backend.feature.payment.domain.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    READY("ready"),      // 가상계좌 발급
    PAID("paid"),        // 결제완료
    FAILED("failed"),    // 결제실패
    CANCELLED("cancelled");  // 환불

    private final String value;

    public static PaymentStatus fromValue(String value) {
        return Arrays.stream(PaymentStatus.values())
                .filter(status -> status.getValue().equalsIgnoreCase(value))
                .findFirst()
                .orElse(null);
    }
}
