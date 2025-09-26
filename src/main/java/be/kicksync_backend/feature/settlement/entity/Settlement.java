package be.kicksync_backend.feature.settlement.entity;

import be.kicksync_backend.common.entity.BaseTimeEntity;
import be.kicksync_backend.feature.order.entity.Order;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "settlements")
public class Settlement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "partner_id", nullable = false)
    private Long partnerId;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private SettlementStatus status = SettlementStatus.PENDING;

    @Column(name = "settlement_date")
    private LocalDate settlementDate;
}
