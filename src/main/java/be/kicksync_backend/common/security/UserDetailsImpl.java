package be.kicksync_backend.common.security;

import be.kicksync_backend.feature.user.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserDetailsImpl implements UserDetails {

    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Creates a new UserDetailsImpl with the supplied credentials and authorities.
     *
     * @param username    the user's username used as the principal
     * @param password    the user's password (typically already encoded)
     * @param authorities collection of granted authorities (roles/permissions) for the user
     */
    public UserDetailsImpl(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }

    /**
     * Creates a UserDetailsImpl from a domain User.
     *
     * Converts the user's role into a single SimpleGrantedAuthority (using the role's name)
     * and constructs a UserDetailsImpl populated with the user's username, password, and authorities.
     *
     * @param user the domain User to adapt into Spring Security's UserDetails; must provide username, password and role
     * @return a UserDetailsImpl representing the given user for Spring Security authentication/authorization
     */
    public static UserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole().name()));
        return new UserDetailsImpl(
                user.getUsername(),
                user.getPassword(),
                authorities);
    }

    /**
     * Returns the authorities granted to the user.
     *
     * @return a collection of GrantedAuthority representing the user's roles/permissions
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * Returns the password used for authentication.
     *
     * @return the user's password (typically a hashed password, may be null for accounts that do not use password authentication)
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Returns the username used to authenticate and identify the user.
     *
     * @return the user's username
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Indicates whether the user's account has not expired.
     *
     * This implementation treats all accounts as non-expiring and always returns {@code true}.
     *
     * @return {@code true} if the account is considered non-expired
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user's account is not locked.
     *
     * @return true if the account is not locked (always true for this implementation)
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indicates whether the user's credentials (password) are non-expired.
     *
     * This implementation always returns true, meaning credentials are considered non-expired.
     *
     * @return true if the user's credentials are non-expired
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is enabled.
     *
     * Always returns {@code true}; all users are treated as enabled.
     *
     * @return {@code true} if the user is enabled
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
} 