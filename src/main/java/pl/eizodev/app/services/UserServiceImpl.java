package pl.eizodev.app.services;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import pl.eizodev.app.entities.Stock;
import pl.eizodev.app.entities.User;
import pl.eizodev.app.repositories.UserRepository;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User findByName(String name) {
        return userRepository.findByName(name);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void saveUser(User user) {
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        user.setActive(1);
        user.setUserUpdate(LocalDate.now());
        user.setRole("USER");
        user.setWalletValue(user.getBalanceAvailable());
        user.setPrevWalletValue(user.getWalletValue());

        userRepository.save(user);
    }

    @Override
    public void updateUser(String username) {
        User user = userRepository.findByName(username);
        List<Stock> userStocks = user.getUserStock();

        if (!userStocks.isEmpty()) {
            BigDecimal stockValue = new BigDecimal(0);

            for (Stock stock : userStocks) {
                stockValue = stockValue.add(stock.getPrice().multiply(BigDecimal.valueOf(stock.getQuantity())));
            }
            user.setStockValue(stockValue);
        } else {
            user.setStockValue(new BigDecimal(0));
        }

        if (LocalDate.now().isAfter(user.getUserUpdate())) {
            user.setPrevWalletValue(user.getWalletValue());
        }

        user.setUserUpdate(LocalDate.now());
        user.setWalletValue(user.getStockValue().add(user.getBalanceAvailable()));
        user.setWalletPercChange((user.getWalletValue().subtract(user.getPrevWalletValue())).divide(user.getPrevWalletValue(), RoundingMode.UNNECESSARY));
    }

    @Override
    public void deleteUser(String email) {
        userRepository.delete(userRepository.findByEmail(email));
    }
}