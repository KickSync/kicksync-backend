package be.kicksync_backend.feature.payment.entity;

import be.kicksync_backend.common.entity.BaseTimeEntity;
import be.kicksync_backend.feature.user.entity.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payments")
public class Payment extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal paymentAmount;

    @Column(nullable = false)
    private LocalDateTime paymentDate;

    @Column(nullable = false, unique = true, length = 100)
    private String impUid;

    @Column(nullable = false, length = 50)
    private String paymentMethod;

    @Column(nullable = false, unique = true, length = 100)
    private String merchantUid;

    @Column(nullable = false, length = 50)
    private String pgProvider;

    @Column(nullable = false, length = 50)
    private String pgType;

    @Column(nullable = false, length = 100)
    private String pgTid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(length = 50)
    private String cardName;

    @Column(length = 4)
    private String cardNumber;

    @Column(length = 255)
    private String cancelReason;

    private LocalDateTime cancelledAt;

    private LocalDateTime requestedAt;

    private LocalDateTime approvedAt;

    @Builder
    public Payment(Long id, User user, BigDecimal paymentAmount,
                   LocalDateTime paymentDate, String impUid, String paymentMethod, String merchantUid,
                   String pgProvider, String pgType, String pgTid, PaymentStatus status,
                   String cardName, String cardNumber, LocalDateTime requestedAt, LocalDateTime approvedAt) {
        this.id = id;
        this.user = user;
        this.paymentAmount = paymentAmount;
        this.paymentDate = paymentDate;
        this.impUid = impUid;
        this.paymentMethod = paymentMethod;
        this.merchantUid = merchantUid;
        this.pgProvider = pgProvider;
        this.pgType = pgType;
        this.pgTid = pgTid;
        this.status = status;
        this.cardName = cardName;
        this.cardNumber = cardNumber;
        this.requestedAt = requestedAt;
        this.approvedAt = approvedAt;
    }

    public void updateOnCancel(String reason) {
        this.status = PaymentStatus.CANCELLED;
        this.cancelReason = reason;
        this.cancelledAt = LocalDateTime.now();
    }
}