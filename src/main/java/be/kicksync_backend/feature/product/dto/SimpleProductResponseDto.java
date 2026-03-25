package be.kicksync_backend.feature.product.dto;

import be.kicksync_backend.feature.product.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class SimpleProductResponseDto {
    @Schema(description = "상품명", example = "Nike Air Max")
    private final String name;
    
    @Schema(description = "모델명", example = "AM-2024-001")
    private final String model;

    public SimpleProductResponseDto(Product product) {
        this.name = product.getName();
        this.model = product.getModel();
    }
} 