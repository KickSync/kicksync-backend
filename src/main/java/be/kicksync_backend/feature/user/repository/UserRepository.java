package be.kicksync_backend.feature.user.repository;

import be.kicksync_backend.feature.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    /**
 * Retrieves a user by their username.
 *
 * @param username the username to look up
 * @return an {@code Optional} containing the found {@link User}, or {@code Optional.empty()} if no user exists with that username
 */
Optional<User> findByUsername(String username);
} 