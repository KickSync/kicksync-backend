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

    /**
     * Retrieve a persisted refresh token by its token string.
     *
     * @param token the refresh token string to look up
     * @return an Optional containing the matching RefreshToken if found, otherwise Optional.empty()
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Creates, persists, and returns a new refresh token for the given user.
     *
     * The created refresh token is tied to the provided user, uses a randomly generated UUID
     * as the token string, and has an expiry set to now plus the configured refresh-token duration.
     *
     * @param user the user the refresh token will be associated with
     * @return the persisted RefreshToken with token string and expiry date populated
     */
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Rotates a refresh token: validates the provided token, deletes it, and issues a new access token and refresh token.
     *
     * @param token the refresh-token string to validate and rotate
     * @return an Optional containing a JwtResponseDto with a new access token and new refresh token if the provided token exists; Optional.empty() if not found
     * @throws CustomException if the provided refresh token has expired (the expired token is removed before the exception is thrown)
     */
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


    /**
     * Ensures the given refresh token is still valid.
     *
     * If the token is expired, it is removed from persistence and a {@link CustomException}
     * with {@link ErrorCode#EXPIRED_TOKEN} is thrown.
     *
     * @param token the refresh token to check
     * @throws CustomException when the token has expired (error code EXPIRED_TOKEN)
     */
    private void verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        }
    }

} 