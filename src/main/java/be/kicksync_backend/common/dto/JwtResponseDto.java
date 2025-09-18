package be.kicksync_backend.common.dto;

import lombok.Getter;

@Getter
public class JwtResponseDto {
    private final String accessToken;
    private final String refreshToken;

    /**
     * Creates an immutable JWT response containing an access token and a refresh token.
     *
     * @param accessToken  the JWT access token to be used for authenticating requests (short-lived)
     * @param refreshToken the JWT refresh token used to obtain new access tokens (long-lived)
     */
    public JwtResponseDto(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
} 