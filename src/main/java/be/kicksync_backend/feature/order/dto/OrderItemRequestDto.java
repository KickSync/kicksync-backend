package be.kicksync_backend.feature.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequestDto {
    @Schema(description = "주문 상품 ID", example = "100")
    @NotNull(message = "상품 ID는 필수입니다.")
    private Long productId;

    @Schema(description = "주문 수량", example = "1")
    @NotNull(message = "수량은 필수입니다.")
    @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
    private Integer quantity;
}
