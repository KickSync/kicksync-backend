package be.kicksync_backend.feature.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DropResultDto {
    @Schema(description = "결과 상태", example = "SUCCESS")
    @NotBlank
    private String status;
    
    @Schema(description = "결과 메시지", example = "당첨되었습니다!")
    @NotBlank
    private String message;
} 