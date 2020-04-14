package pl.eizodev.app.services;

import pl.eizodev.app.entity.User;

import java.util.Optional;

public interface UserService {
    Optional<User> findById(Long id);
    User findByName(String name);
    User findByEmail(String email);
    void saveUser(User user);
    void updateUser(String name);
    void deleteUser(String email);
}