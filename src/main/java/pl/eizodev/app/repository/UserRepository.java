package pl.eizodev.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.eizodev.app.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    public User findByName(String name);
    public User findByEmail(String email);
}