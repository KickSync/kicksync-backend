package be.kicksync_backend.feature.user.service;

import be.kicksync_backend.common.dto.JwtResponseDto;
import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import be.kicksync_backend.common.security.UserDetailsImpl;
import be.kicksync_backend.common.service.RateLimitService;
import be.kicksync_backend.common.service.RedisTokenService;
import be.kicksync_backend.common.util.JwtUtil;
import be.kicksync_backend.feature.token.RefreshTokenService;
import be.kicksync_backend.feature.user.dto.UserResponseDto;
import be.kicksync_backend.feature.user.dto.UserSignupRequestDto;
import be.kicksync_backend.feature.user.entity.User;
import be.kicksync_backend.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import be.kicksync_backend.feature.user.dto.UserLoginRequestDto;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final RateLimitService rateLimitService;
    private final RedisTokenService redisTokenService;

    public UserResponseDto signup(UserSignupRequestDto requestDto) {

        if (userRepository.findByUsername(requestDto.getUsername()).isPresent()) {
            throw new CustomException(ErrorCode.USER_ALREADY_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());
        User user = new User(requestDto.getUsername(), encodedPassword);
        User savedUser = userRepository.save(user);

        return new UserResponseDto(savedUser);
    }

    public JwtResponseDto login(UserLoginRequestDto requestDto) {
        rateLimitService.checkRateLimit(requestDto.getUsername());

        User user = userRepository.findByUsername(requestDto.getUsername()).orElse(null);
        if (user == null || !passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        rateLimitService.resetRateLimit(requestDto.getUsername());

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        String accessToken = jwtUtil.generateAccessToken(userDetails);

        refreshTokenService.deleteRefreshToken(user.getId());
        String refreshToken = refreshTokenService.createRefreshToken(user);

        return new JwtResponseDto(accessToken, refreshToken);
    }

    public void logout(UserDetailsImpl userDetails, String accessToken) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        refreshTokenService.deleteRefreshToken(user.getId());

        Long remainingExpiration = jwtUtil.getRemainingExpirationMs(accessToken);
        redisTokenService.blacklistAccessToken(accessToken, remainingExpiration);
    }

    @CacheEvict(value = "users", key = "#userDetails.username")
    public void deleteAccount(UserDetailsImpl userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        refreshTokenService.deleteRefreshToken(user.getId());

        userRepository.delete(user);
    }

    @Override
    @Cacheable(value = "users", key = "#username")
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .map(UserDetailsImpl::build)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}