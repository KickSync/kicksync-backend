package be.kicksync_backend.feature.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DropResultDto {
    private String status;
    private String message;
} 