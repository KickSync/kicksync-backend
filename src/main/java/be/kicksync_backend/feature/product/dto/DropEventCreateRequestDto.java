package be.kicksync_backend.feature.product.dto;

import be.kicksync_backend.feature.product.entity.DropEventStatus;
import be.kicksync_backend.feature.product.entity.DropEventType;
import be.kicksync_backend.feature.product.entity.DropEvent;
import be.kicksync_backend.feature.product.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DropEventCreateRequestDto {
    @Schema(description = "드롭 이벤트 타입", example = "FCFS")
    private DropEventType type;
    
    @Schema(description = "시작 시간")
    private LocalDateTime startTime;
    
    @Schema(description = "종료 시간")
    private LocalDateTime endTime;
    
    @Schema(description = "상태", example = "SCHEDULED")
    private DropEventStatus status;
    
    @Schema(description = "연관 상품 ID", example = "100")
    private Long productId;

    public DropEvent toEntity(Product product) {
        return DropEvent.builder()
                .type(this.type)
                .startTime(this.startTime)
                .endTime(this.endTime)
                .status(this.status)
                .product(product)
                .build();
    }
}
