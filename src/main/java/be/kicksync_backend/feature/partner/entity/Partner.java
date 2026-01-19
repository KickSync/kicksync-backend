package be.kicksync_backend.feature.partner.entity;

import be.kicksync_backend.common.entity.BaseTimeEntity;
import be.kicksync_backend.feature.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "partners")
public class Partner extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20)
    private String businessNumber;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal commissionRate; // 수수료

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public Partner(String name, String businessNumber, BigDecimal commissionRate, User user) {
        this.name = name;
        this.businessNumber = businessNumber;
        this.commissionRate = commissionRate;
        this.user = user;
    }
}
