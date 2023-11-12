package com.s1gawron.stockexchange.user.model;

import com.s1gawron.stockexchange.user.dto.UserDTO;
import com.s1gawron.stockexchange.user.dto.UserRegisterDTO;
import com.s1gawron.stockexchange.user.dto.UserWalletDTO;

import javax.persistence.*;

@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "enabled")
    private boolean enabled;

    @Column(name = "username")
    private String username;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private UserRole role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private UserWallet userWallet;

    //TODO - add relation with transaction after transaction entity refactor

    protected User() {
    }

    private User(final boolean enabled, final String username, final String email, final String password, final UserRole role) {
        this.enabled = enabled;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
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

    public boolean isEnabled() {
        return enabled;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public UserRole getRole() {
        return role;
    }

    public UserWallet getUserWallet() {
        return userWallet;
    }
}