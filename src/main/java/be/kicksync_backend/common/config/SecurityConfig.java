package be.kicksync_backend.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;

    /**
     * Exposes the application's AuthenticationManager by delegating to the provided AuthenticationConfiguration.
     *
     * @return the AuthenticationManager from the AuthenticationConfiguration
     * @throws Exception if the AuthenticationManager cannot be obtained from the AuthenticationConfiguration
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Configures and returns the application's SecurityFilterChain for HTTP security.
     *
     * <p>Disables CSRF, sets session management to stateless, permits unauthenticated access to
     * the signup/login and token refresh endpoints, restricts `/api/admin/**` to users with the
     * `ADMIN` role, requires authentication for all other requests, registers the configured
     * AuthenticationProvider, and inserts the JWT authentication filter before the
     * UsernamePasswordAuthenticationFilter.</p>
     *
     * @return the built SecurityFilterChain
     * @throws Exception if an error occurs while configuring the HttpSecurity
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/signup", "/api/users/login").permitAll()
                        .requestMatchers("/api/admin/signup", "/api/admin/login").permitAll()
                        .requestMatchers("/api/token/refresh").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
