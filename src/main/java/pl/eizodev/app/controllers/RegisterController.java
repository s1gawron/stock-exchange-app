package pl.eizodev.app.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.eizodev.app.dto.UserRegisterDTO;
import pl.eizodev.app.entities.User;
import pl.eizodev.app.repositories.UserRepository;
import pl.eizodev.app.services.UserService;
import pl.eizodev.app.validators.RegisterValidator;

import java.util.Optional;


@RestController
@AllArgsConstructor
@RequestMapping("user")
class RegisterController {

    private final UserRepository userRepository;
    private final UserService userService;

    @PostMapping("register")
    public ResponseEntity<?> processRegisterForm(@RequestBody final UserRegisterDTO userRegisterDTO, final BindingResult result) {

        new RegisterValidator().validate(User.registerOf(userRegisterDTO), result);

        final Optional<User> userNameExistOptional = userRepository.findByName(userRegisterDTO.getName());
        userNameExistOptional.ifPresent(name -> new RegisterValidator().userNameExist(name, result));

        final Optional<User> userEmailExistOptional = userRepository.findByEmail(userRegisterDTO.getEmail());
        userEmailExistOptional.ifPresent(email -> new RegisterValidator().userEmailExist(email, result));

        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors());
        } else {
            userService.saveUser(User.registerOf(userRegisterDTO));
            return ResponseEntity.ok().body(userRegisterDTO);
        }
    }
}