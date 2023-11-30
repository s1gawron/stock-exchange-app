package com.s1gawron.stockexchange.user.model;

import com.s1gawron.stockexchange.user.dto.UserDTO;
import com.s1gawron.stockexchange.user.dto.UserRegisterDTO;
import com.s1gawron.stockexchange.user.dto.UserWalletDTO;

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
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "active")
    private boolean active;

    @Column(name = "username")
    private String username;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role")
    private UserRole userRole;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private UserWallet userWallet;

    //TODO - add relation with transaction after transaction entity refactor

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

    public void setUserWallet(final UserWallet userWallet) {
        this.userWallet = userWallet;
    }

    public UserDTO toUserDTO() {
        final UserWalletDTO userWalletDTO = userWallet.toUserWalletDTO();
        return new UserDTO(username, email, userWalletDTO);
    }

    public Long getUserId() {
        return userId;
    }

    public UserRole getUserRole() {
        return userRole;
    }

    public UserWallet getUserWallet() {
        return userWallet;
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

    public String getEmail() {
        return email;
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(userRole.name()));
    }

    @Override public boolean isEnabled() {
        return active;
    }
}