package com.s1gawron.stockexchange.user.service;

import com.s1gawron.stockexchange.user.dto.UserDTO;
import com.s1gawron.stockexchange.user.dto.UserRegisterDTO;
import com.s1gawron.stockexchange.user.dto.validator.UserDTOValidator;
import com.s1gawron.stockexchange.user.exception.UserEmailExistsException;
import com.s1gawron.stockexchange.user.exception.UserNameExistsException;
import com.s1gawron.stockexchange.user.model.User;
import com.s1gawron.stockexchange.user.model.UserWallet;
import com.s1gawron.stockexchange.user.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

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
        UserDTOValidator.I.validate(userRegisterDTO);

        final Optional<User> userNameExistOptional = getUser(userRegisterDTO.username());

        if (userNameExistOptional.isPresent()) {
            throw UserNameExistsException.create();
        }

        final Optional<User> userEmailExistOptional = userRepository.findByEmail(userRegisterDTO.email());

        if (userEmailExistOptional.isPresent()) {
            throw UserEmailExistsException.create();
        }

        final String encryptedPassword = new BCryptPasswordEncoder().encode(userRegisterDTO.password());
        final User user = User.createUser(userRegisterDTO, encryptedPassword);
        final UserWallet userWallet = UserWallet.createNewUserWallet(user, userRegisterDTO.userWalletBalance());

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