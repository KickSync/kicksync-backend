package be.kicksync_backend.feature.token;

import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import be.kicksync_backend.common.security.UserDetailsImpl;
import be.kicksync_backend.common.util.JwtUtil;
import be.kicksync_backend.feature.user.entity.User;
import be.kicksync_backend.feature.user.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import be.kicksync_backend.common.dto.JwtResponseDto;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenDurationMs;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<JwtResponseDto> refreshTokens(String token) {
        return refreshTokenRepository.findByToken(token)
                .map(refreshToken -> {
                    verifyExpiration(refreshToken);
                    User user = refreshToken.getUser();
                    refreshTokenRepository.delete(refreshToken);
                    String newAccessToken = jwtUtil.generateAccessToken(UserDetailsImpl.build(user));
                    String newRefreshToken = createRefreshToken(user).getToken();
                    return new JwtResponseDto(newAccessToken, newRefreshToken);
                });
    }


    private void verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        }
    }

} 