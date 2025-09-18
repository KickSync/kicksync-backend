package be.kicksync_backend.feature.user.service;

import be.kicksync_backend.common.dto.JwtResponseDto;
import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import be.kicksync_backend.common.security.UserDetailsImpl;
import be.kicksync_backend.common.util.JwtUtil;
import be.kicksync_backend.feature.token.RefreshTokenService;
import be.kicksync_backend.feature.user.dto.UserResponseDto;
import be.kicksync_backend.feature.user.dto.UserSignupRequestDto;
import be.kicksync_backend.feature.user.entity.User;
import be.kicksync_backend.feature.user.repository.UserRepository;
import be.kicksync_backend.feature.user.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
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
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Registers a new user.
     *
     * Creates and persists a new User with the username and encoded password from the request
     * and returns a DTO representing the created user.
     *
     * @param requestDto contains the desired username and plaintext password
     * @return a UserResponseDto built from the persisted user
     * @throws CustomException if a user with the same username already exists (ErrorCode.USER_ALREADY_EXISTS)
     */
    public UserResponseDto signup(UserSignupRequestDto requestDto) {

        if (userRepository.findByUsername(requestDto.getUsername()).isPresent()) {
            throw new CustomException(ErrorCode.USER_ALREADY_EXISTS);
        }

        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());
        User user = new User(requestDto.getUsername(), encodedPassword);
        User savedUser = userRepository.save(user);

        return new UserResponseDto(savedUser);
    }

    /**
     * Authenticates a user and returns JWT tokens for the session.
     *
     * <p>Validates the supplied username and password, generates a short-lived access token
     * and creates a persistent refresh token for the authenticated user.</p>
     *
     * @param requestDto container carrying the login credentials (username and password)
     * @return a JwtResponseDto containing the generated access token and refresh token
     * @throws CustomException with ErrorCode.USER_NOT_FOUND if no user exists with the given username
     * @throws CustomException with ErrorCode.INVALID_PASSWORD if the provided password does not match
     */
    public JwtResponseDto login(UserLoginRequestDto requestDto) {
        User user = userRepository.findByUsername(requestDto.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = refreshTokenService.createRefreshToken(user).getToken();

        return new JwtResponseDto(accessToken, refreshToken);
    }

    /**
     * Logs out the specified authenticated user by removing all of their refresh tokens.
     *
     * <p>Finds the User by username from the provided UserDetailsImpl and deletes any refresh tokens
     * associated with that User via the RefreshTokenRepository.</p>
     *
     * @param userDetails the authenticated user's details (used to resolve the User by username)
     * @throws CustomException with ErrorCode.USER_NOT_FOUND if no user exists for the given username
     */
    public void logout(UserDetailsImpl userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        refreshTokenRepository.deleteByUser(user);
    }

    /**
     * Permanently deletes the authenticated user's account and any associated refresh token.
     *
     * Looks up the User by username from the provided UserDetailsImpl; if the user cannot be found
     * a CustomException with ErrorCode.USER_NOT_FOUND is thrown. If present, the user's refresh token
     * is removed from the refreshTokenRepository, and then the user record is deleted from the repository.
     *
     * @param userDetails the authenticated user's details (used to locate the User by username)
     * @throws CustomException if no user exists for the given username (ErrorCode.USER_NOT_FOUND)
     */
    public void deleteAccount(UserDetailsImpl userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);
        userRepository.delete(user);
    }

    /**
     * Loads a user's authentication details by username for Spring Security.
     *
     * Returns a UserDetails built from the persisted User; if no user exists with the
     * given username a CustomException with ErrorCode.USER_NOT_FOUND is thrown.
     *
     * @param username the login username to look up
     * @return a UserDetails instance representing the found user
     * @throws CustomException when no user with the given username exists (ErrorCode.USER_NOT_FOUND)
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .map(UserDetailsImpl::build)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
} 