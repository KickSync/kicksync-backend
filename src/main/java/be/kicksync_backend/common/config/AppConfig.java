package be.kicksync_backend.common.config;

import be.kicksync_backend.common.exception.CustomException;
import be.kicksync_backend.common.exception.ErrorCode;
import be.kicksync_backend.common.security.UserDetailsImpl;
import be.kicksync_backend.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AppConfig {

    private final UserRepository userRepository;

    /**
     * Returns a UserDetailsService that resolves a username to a UserDetails.
     *
     * The returned service looks up the user via the injected repository and maps the
     * result to a UserDetailsImpl using its `build()` method. If no user is found a
     * CustomException with ErrorCode.USER_NOT_FOUND is thrown.
     *
     * @return a UserDetailsService that loads UserDetails by username
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
                .map(UserDetailsImpl::build)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * Creates and configures a DaoAuthenticationProvider used by Spring Security.
     *
     * <p>The provider is wired with this configuration's {@code userDetailsService()} and
     * {@code passwordEncoder()} beans so authentication is performed against the application's
     * user repository with BCrypt password hashing.
     *
     * @return a configured {@link AuthenticationProvider} instance (DaoAuthenticationProvider)
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Provides a PasswordEncoder bean that uses BCrypt hashing.
     *
     * The returned PasswordEncoder is a BCryptPasswordEncoder instance and should be used for encoding and
     * verifying user passwords.
     *
     * @return a BCrypt-based PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
} 