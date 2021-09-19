package pl.eizodev.app.user;

import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import pl.eizodev.app.stock.Stock;
import pl.eizodev.app.user.dto.UserRegisterDTO;
import pl.eizodev.app.user.exception.UserEmailExistsException;
import pl.eizodev.app.user.exception.UserNameExistsException;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void registerUser(final UserRegisterDTO userRegisterDTO) {

        final Optional<User> userNameExistOptional = userRepository.findByName(userRegisterDTO.getName());

        if (userNameExistOptional.isPresent()) {
            throw UserNameExistsException.create();
        }

        final Optional<User> userEmailExistOptional = userRepository.findByEmail(userRegisterDTO.getEmail());

        if (userEmailExistOptional.isPresent()) {
            throw UserEmailExistsException.create();
        }

        saveUser(User.registerOf(userRegisterDTO));
    }

    private void saveUser(final User user) {
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        user.setActive(1);
        user.setUserUpdate(LocalDate.now());
        user.setRole("USER");
        user.setWalletValue(user.getBalanceAvailable());
        user.setPrevWalletValue(user.getWalletValue());
        user.setStockValue(BigDecimal.ZERO);
        user.setWalletPercentageChange(BigDecimal.ZERO);

        userRepository.save(user);
    }

    public void updateUser(final String username) {
        final Optional<User> userOptional = userRepository.findByName(username);

        if (userOptional.isPresent()) {
            final User user = userOptional.get();
            final List<Stock> userStocks = user.getUserStock();

            if (!userStocks.isEmpty()) {
                BigDecimal stockValue = new BigDecimal(0);

                for (final Stock stock : userStocks) {
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
            user.setWalletPercentageChange(((user.getWalletValue().subtract(user.getPrevWalletValue())).divide(user.getPrevWalletValue(), RoundingMode.HALF_DOWN)).multiply(BigDecimal.valueOf(100)));
        }
    }

    public Optional<User> findUserByName(final String username) {
        return userRepository.findByName(username);
    }
}