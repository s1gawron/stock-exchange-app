package pl.eizodev.app.user.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import pl.eizodev.app.user.dto.UserDTO;
import pl.eizodev.app.user.dto.UserRegisterDTO;
import pl.eizodev.app.user.dto.UserWalletDTO;

import javax.persistence.*;

@Entity
@Table(name = "user")
@NoArgsConstructor
@DynamicUpdate
@Getter
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

    private User(final boolean enabled, final String username, final String email, final String password, final UserRole role) {
        this.enabled = enabled;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public static User createUser(final UserRegisterDTO userRegisterDTO, final String encryptedPassword) {
        return new User(true, userRegisterDTO.getUsername(), userRegisterDTO.getEmail(), encryptedPassword, UserRole.USER);
    }

    public void setUserWallet(final UserWallet userWallet) {
        this.userWallet = userWallet;
    }

    public UserDTO toUserDTO() {
        final UserWalletDTO userWalletDTO = userWallet.toUserWalletDTO();
        return new UserDTO(username, email, userWalletDTO);
    }
}