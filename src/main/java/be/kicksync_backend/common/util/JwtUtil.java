package be.kicksync_backend.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    @Value("${jwt.secret.key}")
    private String secret;

    @Value("${jwt.access.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh.expiration}")
    private long refreshTokenExpiration;

    private SecretKey key;

    /**
     * Initializes the signing key used for JWT operations.
     *
     * Decodes the Base64-encoded `secret` configuration value and constructs an HMAC-SHA
     * SecretKey assigned to the instance field `key`. This method is executed after
     * the component's construction and dependency injection (invoked by the container).
     *
     * The `secret` must be a Base64-encoded key of sufficient length for HMAC-SHA signing.
     */
    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }


    /**
     * Extracts the username (JWT subject) from the given JWT.
     *
     * @param token the JWT string to parse
     * @return the subject claim (username) contained in the token, or null if the claim is missing
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration timestamp from the given JWT.
     *
     * @param token the JWT string from which to read the expiration claim
     * @return the token's expiration Date (as stored in the `exp` claim)
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Extracts the "roles" claim from the given JWT.
     *
     * The roles are stored in the token as a single comma-separated string.
     *
     * @param token the JWT from which to read the roles claim
     * @return the roles claim as a comma-separated String, or {@code null} if the claim is not present
     */
    public String getRolesFromToken(String token) {
        return getClaimFromToken(token, claims -> (String) claims.get("roles"));
    }

    /**
     * Extracts a specific claim value from the given JWT by applying the provided resolver.
     *
     * @param token the JWT as a compact serialized string
     * @param claimsResolver a function that maps parsed {@link io.jsonwebtoken.Claims} to the desired value
     * @param <T> the type of the value returned by the resolver
     * @return the value produced by applying {@code claimsResolver} to the token's claims
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parse the given signed JWT and return its claims.
     *
     * Uses the component's configured signing key to validate the token signature and
     * extract the token body as a Claims instance.
     *
     * @param token the compact serialized JWT (JWS)
     * @return the parsed Claims from the token
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    /**
     * Returns true if the JWT's expiration time is before the current time.
     *
     * @param token the JWT to inspect
     * @return true when the token is expired, false otherwise
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * Creates a signed JWT access token for the given user using the configured access-token expiration.
     *
     * @param userDetails the user's details (used as the token subject and to derive the `roles` claim)
     * @return a compact JWT string representing the access token
     */
    public String generateAccessToken(UserDetails userDetails) {
        return generateToken(userDetails, accessTokenExpiration);
    }

    /**
     * Generates a signed JWT refresh token for the given user.
     *
     * The token's subject is set to the user's username and it includes the user's
     * roles as a "roles" claim. The token's expiration is set using the
     * configured refresh token lifetime.
     *
     * @param userDetails the authenticated user's details; used to set the token subject and roles
     * @return a signed JWT refresh token as a String
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return generateToken(userDetails, refreshTokenExpiration);
    }

    /**
     * Builds a signed JWT for the given user with the specified lifetime.
     *
     * The token's subject is set to the user's username, `iat` to the current time,
     * and `exp` to current time plus {@code expirationTime}. The user's authorities
     * are stored as a comma-separated string in the "roles" claim.
     *
     * @param userDetails     the authenticated user's details (used for subject and roles)
     * @param expirationTime  token validity duration in milliseconds
     * @return                a compact signed JWT string
     */
    private String generateToken(UserDetails userDetails, long expirationTime) {
        Map<String, Object> claims = new HashMap<>();
        String authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        claims.put("roles", authorities);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key)
                .compact();
    }

    /**
     * Validate that a JWT belongs to the supplied user and is not expired.
     *
     * @param token the JWT string to validate
     * @param userDetails the user whose username must match the token's subject
     * @return true if the token's subject equals {@code userDetails.getUsername()} and the token is not expired
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
} 