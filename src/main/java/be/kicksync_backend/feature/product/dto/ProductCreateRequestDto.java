package be.kicksync_backend.feature.product.dto;

import be.kicksync_backend.feature.product.entity.Product;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCreateRequestDto {
    @NotBlank(message = "상품명은 필수입니다")
    @Size(max = 100, message = "상품명은 100자 이하여야 합니다")
    private String name;

    @NotBlank(message = "모델명은 필수입니다")
    @Size(max = 50, message = "모델명은 50자 이하여야 합니다")
    private String model;

    @NotBlank(message = "출시일은 필수입니다")
    @PastOrPresent(message = "출시일은 미래일 수 없습니다")
    private LocalDate releaseDate;

    @NotBlank(message = "소매가는 필수입니다")
    @DecimalMin(value = "0.0", inclusive = false, message = "소매가는 0보다 커야 합니다")
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