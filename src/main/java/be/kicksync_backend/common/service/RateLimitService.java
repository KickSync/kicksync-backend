package be.kicksync_backend.common.service;

import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    private static final int MAX_ATTEMPTS = 10;
    private static final long WINDOW_SECONDS = 60;

    public void checkRateLimit(String identifier) {
        String key = RATE_LIMIT_PREFIX + identifier;
        String countStr = redisTemplate.opsForValue().get(key);

        int count = countStr != null ? Integer.parseInt(countStr) : 0;

        if (count >= MAX_ATTEMPTS) {
            throw new CustomException(ErrorCode.TOO_MANY_REQUESTS);
        }

        if (count == 0) {
            redisTemplate.opsForValue().set(key, "1", WINDOW_SECONDS, TimeUnit.SECONDS);
        } else {
            redisTemplate.opsForValue().increment(key);
        }
    }

    public void resetRateLimit(String identifier) {
        String key = RATE_LIMIT_PREFIX + identifier;
        redisTemplate.delete(key);
    }
}
