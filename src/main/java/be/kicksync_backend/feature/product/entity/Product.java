package be.kicksync_backend.feature.product.entity;

import be.kicksync_backend.common.entity.BaseTimeEntity;
import be.kicksync_backend.feature.order.entity.Order;
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

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<DropEvent> dropEvents = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<Order> orders = new ArrayList<>();

    public void update(String name, String model, LocalDate releaseDate, BigDecimal retailPrice) {
        this.name = name;
        this.model = model;
        this.releaseDate = releaseDate;
        this.retailPrice = retailPrice;
    }
}
