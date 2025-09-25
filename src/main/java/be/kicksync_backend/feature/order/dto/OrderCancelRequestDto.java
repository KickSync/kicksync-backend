package be.kicksync_backend.feature.order.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderCancelRequestDto {
    @Size(max = 255, message = "취소 사유는 255자 이하여야 합니다.")
    private String reason;
}
