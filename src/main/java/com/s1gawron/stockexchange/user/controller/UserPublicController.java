package com.s1gawron.stockexchange.user.controller;

import com.s1gawron.stockexchange.user.dto.AuthenticationResponseDTO;
import com.s1gawron.stockexchange.user.dto.UserLoginDTO;
import com.s1gawron.stockexchange.user.dto.UserRegisterDTO;
import com.s1gawron.stockexchange.user.service.UserAuthenticationService;
import com.s1gawron.stockexchange.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/public/user/v1")
public class UserPublicController extends UserErrorHandlerController {

    private final UserService userService;

    private final UserAuthenticationService userAuthenticationService;

    public UserPublicController(final UserService userService, final UserAuthenticationService userAuthenticationService) {
        this.userService = userService;
        this.userAuthenticationService = userAuthenticationService;
    }

    @PostMapping("login")
    public AuthenticationResponseDTO loginUser(@RequestBody final UserLoginDTO userLoginDTO) {
        return userAuthenticationService.loginUser(userLoginDTO);
    }

    @PostMapping("register")
    public ResponseEntity registerUser(@RequestBody final UserRegisterDTO userRegisterDTO) {
        userService.validateAndRegisterUser(userRegisterDTO);
        return ResponseEntity.ok().build();
    }

}
