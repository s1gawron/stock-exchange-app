package pl.eizodev.app.services;

import pl.eizodev.app.entity.User;

public interface UserService {
    public User findByName(String name);
    public User findByEmail(String email);
    public void saveUser(User user);
}