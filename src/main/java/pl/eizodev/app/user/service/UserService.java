package pl.eizodev.app.user.service;

import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.eizodev.app.user.dto.UserDTO;
import pl.eizodev.app.user.dto.UserRegisterDTO;
import pl.eizodev.app.user.exception.UserEmailExistsException;
import pl.eizodev.app.user.exception.UserNameExistsException;
import pl.eizodev.app.user.model.User;
import pl.eizodev.app.user.model.UserWallet;
import pl.eizodev.app.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Optional<User> getUser(final String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public void deleteUser(final String username) {
        getUser(username).ifPresent(userRepository::delete);
    }

    @Transactional
    public UserDTO validateAndRegisterUser(final UserRegisterDTO userRegisterDTO) {
        userRegisterDTO.validate();

        final Optional<User> userNameExistOptional = getUser(userRegisterDTO.getUsername());

        if (userNameExistOptional.isPresent()) {
            throw UserNameExistsException.create();
        }

        final Optional<User> userEmailExistOptional = userRepository.findByEmail(userRegisterDTO.getEmail());

        if (userEmailExistOptional.isPresent()) {
            throw UserEmailExistsException.create();
        }

        final String encryptedPassword = new BCryptPasswordEncoder().encode(userRegisterDTO.getPassword());
        final User user = User.createUser(userRegisterDTO, encryptedPassword);
        final UserWallet userWallet = UserWallet.createNewUserWallet(user, userRegisterDTO.getUserWalletBalance());

        user.setUserWallet(userWallet);
        userRepository.save(user);

        return user.toUserDTO();
    }

    @Transactional(readOnly = true)
    public List<String> getAllUsernames() {
        return userRepository.findAll().stream()
            .map(User::getUsername)
            .collect(Collectors.toList());
    }
}