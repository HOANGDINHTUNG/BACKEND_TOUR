package com.wedservice.backend.module.auth.security;

import com.wedservice.backend.module.user.entity.Role;
import com.wedservice.backend.module.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long userId;
    private final String fullName;
    private final String email;
    private final String password;
    private final boolean active;
    private final Role role;
    private final List<? extends GrantedAuthority> authorities;

    private CustomUserDetails(
            Long userId,
            String fullName,
            String email,
            String password,
            boolean active,
            Role role,
            List<? extends GrantedAuthority> authorities
    ) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.active = active;
        this.role = role;
        this.authorities = authorities;
    }

    public static CustomUserDetails fromUser(User user) {
        return new CustomUserDetails(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPassword(),
                Boolean.TRUE.equals(user.getActive()),
                user.getRole(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
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
        return active;
    }
}
