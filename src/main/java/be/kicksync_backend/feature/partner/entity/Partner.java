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
    private String businessNumber; // 사업자등록번호

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal commissionRate; // 수수료율

    @Column(nullable = false, length = 100)
    private String contactEmail; // 담당자 이메일

    @Column(nullable = false, length = 50)
    private String bankName; // 정산 은행

    @Column(nullable = false, length = 50)
    private String accountNumber; // 계좌 번호

    @Column(nullable = false, length = 50)
    private String accountHolder; // 예금주

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public Partner(String name, String businessNumber, BigDecimal commissionRate, String contactEmail, String bankName, String accountNumber, String accountHolder, User user) {
        this.name = name;
        this.businessNumber = businessNumber;
        this.commissionRate = commissionRate;
        this.contactEmail = contactEmail;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.user = user;
    }
}
