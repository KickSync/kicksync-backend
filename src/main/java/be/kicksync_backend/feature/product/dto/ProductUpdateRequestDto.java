package be.kicksync_backend.feature.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
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
public class ProductUpdateRequestDto {
    @Schema(description = "상품명", example = "Updated Kicks")
    @NotBlank(message = "상품명은 필수 항목입니다.")
    @Size(max = 100, message = "상품명은 100자를 초과할 수 없습니다.")
    private String name;

    @Schema(description = "모델명", example = "NK-2024-002")
    @NotBlank(message = "모델명은 필수 항목입니다.")
    @Size(max = 50, message = "모델명은 50자를 초과할 수 없습니다.")
    private String model;

    @Schema(description = "출시일", example = "2024-02-01")
    @NotNull(message = "출시일은 필수 항목입니다.")
    @PastOrPresent(message = "출시일은 미래일 수 없습니다.")
    private LocalDate releaseDate;

    @Schema(description = "소매가", example = "120000")
    @NotNull(message = "소매가는 필수 항목입니다.")
    @Positive(message = "소매가는 0보다 커야 합니다.")
    private BigDecimal retailPrice;

    @Schema(description = "재고 수량", example = "150")
    @NotNull(message = "재고 수량은 필수입니다.")
    @Min(value = 0, message = "재고는 0개 이상이어야 합니다.")
    private Integer stock;
}
