package be.kicksync_backend.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String REFRESH_TOKEN_LOOKUP_PREFIX = "refresh_token_lookup:";
    private static final String BLACKLIST_PREFIX = "blacklist:";

    public void storeRefreshToken(Long userId, String refreshToken, Long expirationMs) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        String lookupKey = REFRESH_TOKEN_LOOKUP_PREFIX + refreshToken;

        redisTemplate.execute(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) {
                operations.watch(key);
                String oldToken = (String) operations.opsForValue().get(key);
                
                operations.multi();
                if (oldToken != null) {
                    operations.delete(REFRESH_TOKEN_LOOKUP_PREFIX + oldToken);
                }
                operations.opsForValue().set(key, refreshToken, expirationMs, TimeUnit.MILLISECONDS);
                operations.opsForValue().set(lookupKey, String.valueOf(userId), expirationMs, TimeUnit.MILLISECONDS);
                return operations.exec();
            }
        });
    }

    public String getRefreshToken(Long userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        return redisTemplate.opsForValue().get(key);
    }

    public Long getUserIdByRefreshToken(String refreshToken) {
        String userIdStr = redisTemplate.opsForValue().get(REFRESH_TOKEN_LOOKUP_PREFIX + refreshToken);
        return userIdStr != null ? Long.parseLong(userIdStr) : null;
    }

    public boolean validateRefreshToken(Long userId, String refreshToken) {
        String storedToken = getRefreshToken(userId);
        return storedToken != null && storedToken.equals(refreshToken);
    }

    public void deleteRefreshToken(Long userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        
        redisTemplate.execute(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) {
                operations.watch(key);
                String token = (String) operations.opsForValue().get(key);
                
                operations.multi();
                if (token != null) {
                    operations.delete(REFRESH_TOKEN_LOOKUP_PREFIX + token);
                }
                operations.delete(key);
                return operations.exec();
            }
        });
    }

    public void blacklistAccessToken(String accessToken, Long expirationMs) {
        String key = BLACKLIST_PREFIX + accessToken;
        // Store with remaining TTL only - auto-expires when token would expire anyway
        redisTemplate.opsForValue().set(key, "blacklisted", expirationMs, TimeUnit.MILLISECONDS);
    }

    public boolean isAccessTokenBlacklisted(String accessToken) {
        String key = BLACKLIST_PREFIX + accessToken;
        return redisTemplate.hasKey(key);
    }
}
