package be.kicksync_backend.feature.settlement.domain.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SettlementStatus {
    PENDING("정산 대기"),
    PROCESSING("정산 처리중"),
    COMPLETED("정산 완료"),
    FAILED("정산 실패");

    private final String description;
}
