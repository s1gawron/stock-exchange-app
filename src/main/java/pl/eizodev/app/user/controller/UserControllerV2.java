package pl.eizodev.app.user.controller;

import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;
import pl.eizodev.app.user.dto.UserDTO;
import pl.eizodev.app.user.dto.UserLoginDTO;
import pl.eizodev.app.user.dto.UserRegisterDTO;
import pl.eizodev.app.user.dto.UserWalletDTO;
import pl.eizodev.app.user.service.UserService;
import pl.eizodev.app.user.service.UserWalletService;

@RestController
@RequestMapping("api/v2/user")
@AllArgsConstructor
public class UserControllerV2 extends UserErrorHandlerController {

    private final UserService userService;

    private final UserWalletService userWalletService;

    //Empty controller for swagger visibility, logic is hidden in jwt/JwtUsernamePasswordAuthenticationFilter
    @PostMapping("login")
    public void loginUser(@RequestBody final UserLoginDTO userLoginDTO) {

    }

    @PostMapping("register")
    public UserDTO registerUser(@RequestBody final UserRegisterDTO userRegisterDTO) {
        return userService.validateAndRegisterUser(userRegisterDTO);
    }

    @GetMapping("wallet")
    public UserWalletDTO getUserWalletDetails(@CurrentSecurityContext(expression = "authentication.name") final String username) {
        return userWalletService.updateAndGetUserWallet(username);
    }
}
