package be.kicksync_backend.feature.token;

import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import be.kicksync_backend.common.security.UserDetailsImpl;
import be.kicksync_backend.common.service.RedisTokenService;
import be.kicksync_backend.common.util.JwtUtil;
import be.kicksync_backend.feature.user.entity.User;
import be.kicksync_backend.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import be.kicksync_backend.common.dto.JwtResponseDto;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final RedisTokenService redisTokenService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenDurationMs;

    public String createRefreshToken(User user) {
        String refreshToken = UUID.randomUUID().toString();
        redisTokenService.storeRefreshToken(user.getId(), refreshToken, refreshTokenDurationMs);
        return refreshToken;
    }

    public Optional<JwtResponseDto> refreshTokens(String token) {
        String normalized = token == null ? "" : token.trim();
        if (normalized.isEmpty()) return Optional.empty();

        Long userId = redisTokenService.getUserIdByRefreshToken(normalized);
        if (userId == null) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        return userRepository.findById(userId)
                .map(user -> {
                    // Delete old token and create new one
                    redisTokenService.deleteRefreshToken(user.getId());
                    String newAccessToken = jwtUtil.generateAccessToken(UserDetailsImpl.build(user));
                    String newRefreshToken = createRefreshToken(user);
                    return new JwtResponseDto(newAccessToken, newRefreshToken);
                })
                .or(() -> {
                    throw new CustomException(ErrorCode.USER_NOT_FOUND);
                });
    }

    public void deleteRefreshToken(Long userId) {
        redisTokenService.deleteRefreshToken(userId);
    }

} 