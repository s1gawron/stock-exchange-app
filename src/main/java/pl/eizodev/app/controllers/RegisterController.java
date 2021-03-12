package pl.eizodev.app.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.eizodev.app.dtos.UserRegisterDTO;
import pl.eizodev.app.services.RegistrationService;


@RestController
@AllArgsConstructor
@RequestMapping("user")
class RegisterController {

    private final RegistrationService registrationService;

    @PostMapping("register")
    public HttpStatus processRegisterForm(@RequestBody final UserRegisterDTO userRegisterDTO) {
        return registrationService.registerUser(userRegisterDTO);
    }
}