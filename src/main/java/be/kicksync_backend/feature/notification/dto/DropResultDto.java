package be.kicksync_backend.feature.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DropResultDto {
    @NotBlank
    private String status;
    @NotBlank
    private String message;
} 