package be.kicksync_backend.feature.product.dto;

import be.kicksync_backend.feature.product.entity.Product;
import lombok.Getter;

@Getter
public class SimpleProductResponseDto {
    private final String name;
    private final String model;

    public SimpleProductResponseDto(Product product) {
        this.name = product.getName();
        this.model = product.getModel();
    }
} 