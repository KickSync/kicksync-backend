package be.kicksync_backend.feature.payment.dto;

import java.math.BigDecimal;

public interface PartnerSettlementDto {
    Long getPartnerId();
    BigDecimal getTotalAmount();
}
