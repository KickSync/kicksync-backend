package be.kicksync_backend.feature.product.dto;

import be.kicksync_backend.feature.product.entity.DropEventStatus;
import be.kicksync_backend.feature.product.entity.DropEventType;
import be.kicksync_backend.feature.product.entity.DropEvent;
import be.kicksync_backend.feature.product.entity.Product;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DropEventCreateRequestDto {
    private DropEventType type;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private DropEventStatus status;
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
