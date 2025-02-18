package com.s1gawron.stockexchange.user.dao;

import com.s1gawron.stockexchange.user.model.User;
import com.s1gawron.stockexchange.user.dao.filter.UserFilterParam;

import java.util.Optional;

public interface UserDAO {

    Optional<User> findByFilter(UserFilterParam filterParam);

    void saveUser(User user);

}
