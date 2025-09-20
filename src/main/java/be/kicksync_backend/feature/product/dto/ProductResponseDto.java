package be.kicksync_backend.feature.product.dto;

import be.kicksync_backend.feature.product.entity.Product;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class ProductResponseDto {
    private final Long id;
    private final String name;
    private final String model;
    private final LocalDate releaseDate;
    private final BigDecimal retailPrice;

    public ProductResponseDto(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.model = product.getModel();
        this.releaseDate = product.getReleaseDate();
        this.retailPrice = product.getRetailPrice();
    }
} 