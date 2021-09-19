package pl.eizodev.app.user.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;
import pl.eizodev.app.stock.StockService;
import pl.eizodev.app.user.User;
import pl.eizodev.app.user.UserService;
import pl.eizodev.app.user.dto.UserDTO;
import pl.eizodev.app.user.dto.UserRegisterDTO;
import pl.eizodev.app.user.exception.AccountNotFoundException;

import java.util.Optional;

@RestController
@RequestMapping("user")
@AllArgsConstructor
public class UserController extends UserErrorHandlerController {

    private final UserService userService;

    private final StockService stockService;

    @PostMapping("register")
    public HttpStatus registerUser(@RequestBody final UserRegisterDTO userRegisterDTO) {
        userService.registerUser(userRegisterDTO);

        return HttpStatus.OK;
    }

    @GetMapping("myWallet")
    public UserDTO getUserWalletDetails(@CurrentSecurityContext(expression = "authentication.name") final String username) {
        stockService.updateUserStockByUsername(username);
        userService.updateUser(username);

        final Optional<User> userOptional = userService.findUserByName(username);

        return userOptional.map(UserDTO::of).orElseThrow(() -> {
            throw AccountNotFoundException.create(username);
        });
    }
}
