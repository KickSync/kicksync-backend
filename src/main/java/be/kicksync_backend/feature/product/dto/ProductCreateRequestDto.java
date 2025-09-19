package be.kicksync_backend.feature.product.dto;

import be.kicksync_backend.feature.product.entity.Product;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class ProductCreateRequestDto {
    private String name;
    private String model;
    private LocalDate releaseDate;
    private BigDecimal retailPrice;

    public Product toEntity() {
        return Product.builder()
                .name(this.name)
                .model(this.model)
                .releaseDate(this.releaseDate)
                .retailPrice(this.retailPrice)
                .build();
    }
} 