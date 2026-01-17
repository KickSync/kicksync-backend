package be.kicksync_backend.feature.product.dto;

import be.kicksync_backend.feature.partner.entity.Partner;
import be.kicksync_backend.feature.product.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "상품명", example = "New Kicks")
    @NotBlank(message = "상품명은 필수입니다")
    @Size(max = 100, message = "상품명은 100자 이하여야 합니다")
    private String name;

    @Schema(description = "모델명", example = "NK-2024-001")
    @NotBlank(message = "모델명은 필수입니다")
    @Size(max = 50, message = "모델명은 50자 이하여야 합니다")
    private String model;

    @Schema(description = "출시일", example = "2024-01-01")
    @NotNull(message = "출시일은 필수입니다")
    @PastOrPresent(message = "출시일은 미래일 수 없습니다")
    private LocalDate releaseDate;

    @Schema(description = "소매가", example = "100000")
    @NotNull(message = "소매가는 필수입니다")
    @DecimalMin(value = "0.0", inclusive = false, message = "소매가는 0보다 커야 합니다")
    private BigDecimal retailPrice;

    @Schema(description = "입점사 ID", example = "1")
    @NotNull(message = "입점사 ID는 필수입니다")
    private Long partnerId;

    public Product toEntity(Partner partner) {
        return Product.builder()
                .name(this.name)
                .model(this.model)
                .releaseDate(this.releaseDate)
                .retailPrice(this.retailPrice)
                .partner(partner)
                .build();
    }
} 