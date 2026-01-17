package be.kicksync_backend.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record JwtResponseDto(
    @Schema(description = "Access Token", example = "eyJhbGciOiJIUzI1NiJ9...")
    String accessToken,
    
    @Schema(description = "Refresh Token", example = "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...")
    String refreshToken
) {
}