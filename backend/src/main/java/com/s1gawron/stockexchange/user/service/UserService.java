package com.s1gawron.stockexchange.user.service;

import com.s1gawron.stockexchange.user.dto.UserRegisterDTO;
import com.s1gawron.stockexchange.user.dto.validator.UserRegisterDTOValidator;
import com.s1gawron.stockexchange.user.exception.UserEmailExistsException;
import com.s1gawron.stockexchange.user.exception.UserNameExistsException;
import com.s1gawron.stockexchange.user.model.User;
import com.s1gawron.stockexchange.user.model.UserWallet;
import com.s1gawron.stockexchange.user.dao.UserDAO;
import com.s1gawron.stockexchange.user.dao.UserWalletDAO;
import com.s1gawron.stockexchange.user.dao.filter.UserFilterParam;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserDAO userDAO;

    private final UserWalletDAO userWalletDAO;

    public UserService(final UserDAO userDAO, final UserWalletDAO userWalletDAO) {
        this.userDAO = userDAO;
        this.userWalletDAO = userWalletDAO;
    }

    @Transactional
    public boolean validateAndRegisterUser(final UserRegisterDTO userRegisterDTO) {
        UserRegisterDTOValidator.I.validate(userRegisterDTO);

        final Optional<User> usernameExist = userDAO.findByFilter(UserFilterParam.createForUsername(userRegisterDTO.username()));

        if (usernameExist.isPresent()) {
            throw UserNameExistsException.create();
        }

        final Optional<User> userEmailExist = userDAO.findByFilter(UserFilterParam.createForEmail(userRegisterDTO.email()));

        if (userEmailExist.isPresent()) {
            throw UserEmailExistsException.create();
        }

        final String encryptedPassword = new BCryptPasswordEncoder().encode(userRegisterDTO.password());
        final User user = User.createUser(userRegisterDTO, encryptedPassword);
        userDAO.saveUser(user);

        final UserWallet userWallet = UserWallet.createNewUserWallet(user.getUserId(), userRegisterDTO.userWalletBalance());
        userWalletDAO.saveUserWallet(userWallet);

        return true;
    }

    @Transactional(readOnly = true)
    public List<Long> getAllUserIds() {
        return userDAO.getAllUserIds();
    }
}