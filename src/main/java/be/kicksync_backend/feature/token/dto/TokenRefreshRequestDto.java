package be.kicksync_backend.feature.token.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
public class TokenRefreshRequestDto {
    @Schema(description = "Refresh Token", example = "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...")
    @NotBlank
    private String refreshToken;
}