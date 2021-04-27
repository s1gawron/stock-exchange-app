package pl.eizodev.app.service;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.eizodev.app.dto.UserRegisterDTO;
import pl.eizodev.app.entity.User;
import pl.eizodev.app.repository.UserRepository;
import pl.eizodev.app.service.exception.UserEmailExistsException;
import pl.eizodev.app.service.exception.UserNameExistsException;

import java.util.Optional;

@Service
@AllArgsConstructor
public class RegistrationService {

    private final UserRepository userRepository;
    private final UserService userService;

    public HttpStatus registerUser(final UserRegisterDTO userRegisterDTO) {

        final Optional<User> userNameExistOptional = userRepository.findByName(userRegisterDTO.getName());

        if (userNameExistOptional.isPresent()) {
            throw UserNameExistsException.create();
        }

        final Optional<User> userEmailExistOptional = userRepository.findByEmail(userRegisterDTO.getEmail());

        if (userEmailExistOptional.isPresent()) {
            throw UserEmailExistsException.create();
        }

        userService.saveUser(User.registerOf(userRegisterDTO));
        return HttpStatus.OK;
    }
}
