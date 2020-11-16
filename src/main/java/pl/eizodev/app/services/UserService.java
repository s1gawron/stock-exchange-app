package pl.eizodev.app.services;

import pl.eizodev.app.entities.User;

public interface UserService {
    void saveUser(User user);
    void updateUser(String username);
    void deleteUser(String email);
}