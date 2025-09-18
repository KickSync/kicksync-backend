package be.kicksync_backend.common.config;

import be.kicksync_backend.common.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    /**
     * Processes an incoming HTTP request to authenticate a JWT (if present) and populate the Spring Security context.
     *
     * <p>If the request's "Authorization" header contains a Bearer token, the method extracts the JWT, resolves the
     * username, loads the corresponding UserDetails, and validates the token. When validation succeeds, an
     * authenticated UsernamePasswordAuthenticationToken is placed into SecurityContextHolder so downstream handlers
     * see the request as authenticated. If the Authorization header is missing or does not start with "Bearer ",
     * the request is passed through without modifying the security context.</p>
     *
     * <p>JWT-related errors (expired, malformed, invalid signature, or illegal argument) are handled by returning a
     * 401 Unauthorized JSON response instead of proceeding with the filter chain.</p>
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            jwt = authHeader.substring(7);
            username = jwtUtil.getUsernameFromToken(jwt);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            setErrorResponse(response, e);
        }
    }

    /**
     * Writes a 401 Unauthorized JSON body to the given HttpServletResponse.
     *
     * The produced JSON contains keys: "status" (401), "error" ("Unauthorized"),
     * "message" (the throwable's message), and "path" (obtained from the request's servlet path).
     *
     * @param response the HTTP response to write the JSON body to
     * @param ex       the exception whose message will be included in the response body
     * @throws IOException if writing to the response output stream fails
     * @throws ClassCastException if the provided response cannot be cast to HttpServletRequest when attempting to obtain the servlet path
     */
    private void setErrorResponse(HttpServletResponse response, Throwable ex) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        final Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "Unauthorized");
        body.put("message", ex.getMessage());
        body.put("path", ((HttpServletRequest) response).getServletPath());

        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }
} 