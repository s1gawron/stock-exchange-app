package pl.eizodev.app.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.eizodev.app.dto.UserRegisterDTO;
import pl.eizodev.app.service.RegistrationService;

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
