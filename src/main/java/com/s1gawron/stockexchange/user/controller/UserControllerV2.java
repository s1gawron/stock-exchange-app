package com.s1gawron.stockexchange.user.controller;

import com.s1gawron.stockexchange.user.dto.UserDTO;
import com.s1gawron.stockexchange.user.dto.UserLoginDTO;
import com.s1gawron.stockexchange.user.dto.UserRegisterDTO;
import com.s1gawron.stockexchange.user.dto.UserWalletDTO;
import com.s1gawron.stockexchange.user.service.UserService;
import com.s1gawron.stockexchange.user.service.UserWalletService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

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
