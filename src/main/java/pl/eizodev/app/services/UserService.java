package pl.eizodev.app.services;

import pl.eizodev.app.entities.User;

import java.util.Optional;

public interface UserService {
    Optional<User> findById(Long id);
    Optional<User> findByName(String name);
    Optional<User> findByEmail(String email);
    void saveUser(User user);
    void updateUser(String username);
    void deleteUser(String email);
}