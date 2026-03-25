package be.kicksync_backend.common.security;

import be.kicksync_backend.feature.user.entity.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDetailsImpl implements UserDetails {
    private Long id;
    private String username;
    private String password;
    private Role role;
    
    @JsonIgnore
    private Collection<? extends GrantedAuthority> authorities;

    public static UserDetailsImpl build(be.kicksync_backend.feature.user.entity.User user) {
        String roleName = user.getRole() != null ? user.getRole().name() : "USER";
        String authority = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(authority));
        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getRole(),
                authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 역직렬화 시 authorities가 null일 경우 role을 기반으로 재생성
        if (authorities == null && role != null) {
            String roleName = role.name();
            String authority = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
            return List.of(new SimpleGrantedAuthority(authority));
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}