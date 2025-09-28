package be.kicksync_backend.feature.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PartnerSettlementDto {
    private Long partnerId;
    private BigDecimal totalAmount;
}