package be.kicksync_backend.feature.settlement.entity;

import be.kicksync_backend.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "settlement_error_logs")
public class SettlementErrorLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "partner_id", nullable = false)
    private Long partnerId;

    @Column(name = "settlement_date", nullable = false)
    private LocalDate settlementDate;

    @Column(name = "failed_amount", precision = 15, scale = 2)
    private BigDecimal failedAmount;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;
}
