package be.kicksync_backend.feature.token.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
 
@Getter
@NoArgsConstructor
public class TokenRefreshRequestDto {
    private String refreshToken;
} 