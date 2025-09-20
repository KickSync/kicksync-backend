package be.kicksync_backend.common.dto;

public record JwtResponseDto(String accessToken, String refreshToken) {
}