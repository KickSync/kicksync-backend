package be.kicksync_backend.feature.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue
    private Long id;
    private String username;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    /**
     * Creates a new User with the given username and password and assigns the default Role.USER.
     *
     * @param username the user's unique username
     * @param password the user's password
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.role = Role.USER;
    }

    /**
     * Create a User with the specified username, password, and role.
     *
     * @param username the user's login name
     * @param password the user's password
     * @param role the role to assign to the user
     */
    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
} 