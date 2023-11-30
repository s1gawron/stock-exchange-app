package com.s1gawron.stockexchange.user.service;

import com.s1gawron.stockexchange.user.dto.UserDTO;
import com.s1gawron.stockexchange.user.dto.UserRegisterDTO;
import com.s1gawron.stockexchange.user.dto.validator.UserDTOValidator;
import com.s1gawron.stockexchange.user.exception.UserEmailExistsException;
import com.s1gawron.stockexchange.user.exception.UserNameExistsException;
import com.s1gawron.stockexchange.user.model.User;
import com.s1gawron.stockexchange.user.model.UserWallet;
import com.s1gawron.stockexchange.user.repository.UserDAO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserDAO userDAO;

    public UserService(final UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Transactional(readOnly = true)
    public Optional<User> getUser(final String username) {
        return userDAO.findByUsername(username);
    }

    @Transactional
    public void deleteUser(final String username) {
        getUser(username).ifPresent(userDAO::deleteUser);
    }

    @Transactional
    public UserDTO validateAndRegisterUser(final UserRegisterDTO userRegisterDTO) {
        UserDTOValidator.I.validate(userRegisterDTO);

        final Optional<User> userNameExistOptional = getUser(userRegisterDTO.username());

        if (userNameExistOptional.isPresent()) {
            throw UserNameExistsException.create();
        }

        final Optional<User> userEmailExistOptional = userDAO.findByEmail(userRegisterDTO.email());

        if (userEmailExistOptional.isPresent()) {
            throw UserEmailExistsException.create();
        }

        final String encryptedPassword = new BCryptPasswordEncoder().encode(userRegisterDTO.password());
        final User user = User.createUser(userRegisterDTO, encryptedPassword);
        final UserWallet userWallet = UserWallet.createNewUserWallet(user, userRegisterDTO.userWalletBalance());

        user.setUserWallet(userWallet);
        userDAO.saveUser(user);

        return user.toUserDTO();
    }

    @Transactional(readOnly = true)
    public List<Long> getAllUserIds() {
        return userDAO.getAllUserIds();
    }
}