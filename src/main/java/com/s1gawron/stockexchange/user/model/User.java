package com.s1gawron.stockexchange.user.model;

import com.s1gawron.stockexchange.user.dto.UserRegisterDTO;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "user")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", unique = true, nullable = false)
    private long userId;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    private UserRole userRole;

    protected User() {
    }

    public User(final boolean active, final String username, final String email, final String password, final UserRole userRole) {
        this.active = active;
        this.username = username;
        this.email = email;
        this.password = password;
        this.userRole = userRole;
    }

    public User(final Long userId, final boolean active, final String username, final String email, final String password, final UserRole userRole) {
        this.userId = userId;
        this.active = active;
        this.username = username;
        this.email = email;
        this.password = password;
        this.userRole = userRole;
    }

    public static User createUser(final UserRegisterDTO userRegisterDTO, final String encryptedPassword) {
        return new User(true, userRegisterDTO.username(), userRegisterDTO.email(), encryptedPassword, UserRole.USER);
    }

    public long getUserId() {
        return userId;
    }

    @Override public String getUsername() {
        return username;
    }

    @Override public String getPassword() {
        return password;
    }

    @Override public boolean isAccountNonExpired() {
        return true;
    }

    @Override public boolean isAccountNonLocked() {
        return true;
    }

    @Override public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(userRole.name()));
    }

    @Override public boolean isEnabled() {
        return active;
    }
}