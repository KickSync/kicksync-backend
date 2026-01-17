package be.kicksync_backend.feature.product.entity;

import be.kicksync_backend.common.entity.BaseTimeEntity;
import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import be.kicksync_backend.feature.order.entity.OrderItem;
import be.kicksync_backend.feature.partner.entity.Partner;
import jakarta.persistence.*;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "products")
public class Product extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String model;

    @Column(nullable = false)
    private LocalDate releaseDate;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal retailPrice;

    @Column(nullable = false)
    private Integer stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    @Builder.Default
    @OneToMany(mappedBy = "product")
    private List<DropEvent> dropEvents = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "product")
    private List<OrderItem> orderItems = new ArrayList<>();

    public void update(String name, String model, LocalDate releaseDate, BigDecimal retailPrice) {
        this.name = name != null ? name.trim() : null;
        this.model = model != null ? model.trim() : null;
        this.releaseDate = releaseDate;
        this.retailPrice = retailPrice;
    }

    public void decreaseStock(Integer quantity) {
        if (this.stock - quantity < 0) {
            throw new CustomException(ErrorCode.INSUFFICIENT_STOCK);
        }
        this.stock -= quantity;
    }

    public void increaseStock(Integer quantity) {
        this.stock += quantity;
    }
}