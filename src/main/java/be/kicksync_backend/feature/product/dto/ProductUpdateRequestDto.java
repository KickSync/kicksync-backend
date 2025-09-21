package be.kicksync_backend.feature.product.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class ProductUpdateRequestDto {
    @NotBlank(message = "상품명은 필수 항목입니다.")
    @Size(max = 100, message = "상품명은 100자를 초과할 수 없습니다.")
    private String name;

    @NotBlank(message = "모델명은 필수 항목입니다.")
    @Size(max = 50, message = "모델명은 50자를 초과할 수 없습니다.")
    private String model;

    @NotNull(message = "출시일은 필수 항목입니다.")
    @PastOrPresent(message = "출시일은 미래일 수 없습니다.")
    private LocalDate releaseDate;

    @NotNull(message = "소매가는 필수 항목입니다.")
    @Positive(message = "소매가는 0보다 커야 합니다.")
    private BigDecimal retailPrice;
}