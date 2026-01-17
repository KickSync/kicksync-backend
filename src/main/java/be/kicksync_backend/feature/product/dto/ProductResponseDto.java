package be.kicksync_backend.feature.product.dto;

import be.kicksync_backend.feature.product.entity.Product;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class ProductResponseDto {
    @Schema(description = "상품 ID (PK)", example = "100")
    private final Long id;

    @Schema(description = "상품명", example = "Nike Air Max")
    private final String name;

    @Schema(description = "모델명", example = "AM-2024-001")
    private final String model;

    @Schema(description = "발매일", example = "2024-01-01")
    private final LocalDate releaseDate;

    @Schema(description = "발매가", example = "159000")
    private final BigDecimal retailPrice;

    public ProductResponseDto(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.model = product.getModel();
        this.releaseDate = product.getReleaseDate();
        this.retailPrice = product.getRetailPrice();
    }

    @JsonCreator
    public ProductResponseDto(@JsonProperty("id") Long id,
                              @JsonProperty("name") String name,
                              @JsonProperty("model") String model,
                              @JsonProperty("releaseDate") LocalDate releaseDate,
                              @JsonProperty("retailPrice") BigDecimal retailPrice) {
        this.id = id;
        this.name = name;
        this.model = model;
        this.releaseDate = releaseDate;
        this.retailPrice = retailPrice;
    }
}