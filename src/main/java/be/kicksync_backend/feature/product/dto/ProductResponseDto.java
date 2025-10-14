package be.kicksync_backend.feature.product.dto;

import be.kicksync_backend.feature.product.entity.Product;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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