package pl.eizodev.app.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.eizodev.app.dtos.UserRegisterDTO;
import pl.eizodev.app.services.RegistrationService;

@RestController
@RequestMapping("user")
@AllArgsConstructor
@CrossOrigin
class UserController extends AbstractErrorHandlerController {

    private final RegistrationService registrationService;

    @PostMapping("register")
    public HttpStatus registerUser(@RequestBody final UserRegisterDTO userRegisterDTO) {
        return registrationService.registerUser(userRegisterDTO);
    }
}
