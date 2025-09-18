package be.kicksync_backend.feature.user.repository;

import be.kicksync_backend.feature.token.RefreshToken;
import be.kicksync_backend.feature.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    /**
 * Finds a refresh token entity by its token string.
 *
 * @param token the token string to look up
 * @return an Optional containing the matching RefreshToken if present, otherwise an empty Optional
 */
Optional<RefreshToken> findByToken(String token);

    /**
 * Finds the refresh token associated with the given user.
 *
 * @param user the user whose refresh token is requested
 * @return an Optional containing the user's RefreshToken if present, otherwise an empty Optional
 */
Optional<RefreshToken> findByUser(User user);

    /**
 * Delete all refresh token records associated with the given user.
 *
 * This issues a delete operation in the persistent store for any RefreshToken entities
 * linked to the provided User.
 *
 * @param user the user whose refresh token(s) should be removed
 */
void deleteByUser(User user);
} 