package be.kicksync_backend.common.dto;

import lombok.Getter;

@Getter
public class JwtResponseDto {
    private final String accessToken;
    private final String refreshToken;

    public JwtResponseDto(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
} 