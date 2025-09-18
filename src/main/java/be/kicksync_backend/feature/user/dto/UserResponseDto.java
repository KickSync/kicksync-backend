package be.kicksync_backend.feature.user.dto;

import be.kicksync_backend.feature.user.entity.User;
import lombok.Getter;

@Getter
public class UserResponseDto {
    private final Long id;
    private final String username;
    private final String role;

    /**
     * Creates a response DTO by mapping values from the given User entity.
     *
     * <p>Sets {@code id}, {@code username}, and {@code role} (the enum name of the user's role).
     *
     * @param user the source User entity to map from; must not be {@code null}. If {@code user.getRole()}
     *             is {@code null} a {@link NullPointerException} may occur when obtaining the role name.
     */
    public UserResponseDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.role = user.getRole().name();
    }
} 