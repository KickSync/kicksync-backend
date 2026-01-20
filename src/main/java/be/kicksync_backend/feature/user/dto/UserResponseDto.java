package be.kicksync_backend.feature.user.dto;

import be.kicksync_backend.feature.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class UserResponseDto {
    @Schema(description = "사용자 ID (PK)", example = "1")
    private final Long id;

    @Schema(description = "사용자 아이디", example = "user123")
    private final String username;

    @Schema(description = "사용자 권한", example = "USER")
    private final String role;

    public UserResponseDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.role = user.getRole().name();
    }
} 