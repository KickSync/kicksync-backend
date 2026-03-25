package be.kicksync_backend.feature.product.dto;

import be.kicksync_backend.feature.product.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDto {
    @Schema(description = "상품 ID (PK)", example = "100")
    private Long id;

    @Schema(description = "상품명", example = "Nike Air Max")
    private String name;

    @Schema(description = "모델명", example = "AM-2024-001")
    private String model;

    @Schema(description = "발매일", example = "2024-01-01")
    private LocalDate releaseDate;

    @Schema(description = "발매가", example = "159000")
    private BigDecimal retailPrice;

    public ProductResponseDto(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.model = product.getModel();
        this.releaseDate = product.getReleaseDate();
        this.retailPrice = product.getRetailPrice();
    }
}