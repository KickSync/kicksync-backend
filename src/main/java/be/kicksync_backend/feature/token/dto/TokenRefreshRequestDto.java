package be.kicksync_backend.feature.token.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
public class TokenRefreshRequestDto {
    @NotBlank
    private String refreshToken;
}