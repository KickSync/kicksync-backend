package be.kicksync_backend.feature.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelRequestDto {
    @Schema(description = "취소 사유", example = "단순 변심")
    @Size(max = 255, message = "취소 사유는 255자 이하여야 합니다.")
    private String reason;
}
