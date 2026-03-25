package be.kicksync_backend.feature.admin.auth.service;

import be.kicksync_backend.common.dto.JwtResponseDto;
import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import be.kicksync_backend.common.security.UserDetailsImpl;
import be.kicksync_backend.common.service.RedisTokenService;
import be.kicksync_backend.common.util.JwtUtil;
import be.kicksync_backend.feature.admin.auth.dto.AdminLoginRequestDto;
import be.kicksync_backend.feature.admin.auth.dto.AdminSignupRequestDto;
import be.kicksync_backend.feature.token.RefreshTokenService;
import be.kicksync_backend.feature.user.dto.UserResponseDto;
import be.kicksync_backend.feature.user.entity.Role;
import be.kicksync_backend.feature.user.entity.User;
import be.kicksync_backend.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminAuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final RedisTokenService redisTokenService;

    public UserResponseDto signup(AdminSignupRequestDto requestDto) {
        if (userRepository.findByUsername(requestDto.getUsername()).isPresent()) {
            throw new CustomException(ErrorCode.USER_ALREADY_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());
        User user = new User(requestDto.getUsername(), encodedPassword, Role.ADMIN);
        User savedUser = userRepository.save(user);

        return new UserResponseDto(savedUser);
    }

    public JwtResponseDto login(AdminLoginRequestDto requestDto) {
        User user = userRepository.findByUsername(requestDto.getUsername()).orElse(null);
        
        if (user == null || !passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        if (user.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        String accessToken = jwtUtil.generateAccessToken(userDetails);

        refreshTokenService.deleteRefreshToken(user.getId());
        String refreshToken = refreshTokenService.createRefreshToken(user);

        return new JwtResponseDto(accessToken, refreshToken);
    }
}