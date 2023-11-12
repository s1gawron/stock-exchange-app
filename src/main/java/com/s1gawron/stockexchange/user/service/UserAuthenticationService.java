package com.s1gawron.stockexchange.user.service;

import com.s1gawron.stockexchange.security.JwtService;
import com.s1gawron.stockexchange.user.dto.AuthenticationResponseDTO;
import com.s1gawron.stockexchange.user.dto.UserLoginDTO;
import com.s1gawron.stockexchange.user.exception.UserNotFoundException;
import com.s1gawron.stockexchange.user.model.User;
import com.s1gawron.stockexchange.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserAuthenticationService {

    private final UserRepository userRepository;

    private final AuthenticationManager authManager;

    private final JwtService jwtService;

    public UserAuthenticationService(final UserRepository userRepository, final AuthenticationManager authManager, final JwtService jwtService) {
        this.userRepository = userRepository;
        this.authManager = authManager;
        this.jwtService = jwtService;
    }

    public AuthenticationResponseDTO loginUser(final UserLoginDTO userLoginDTO) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(userLoginDTO.username(), userLoginDTO.password()));

        final User user = userRepository.findByUsername(userLoginDTO.username()).orElseThrow(() -> UserNotFoundException.create(userLoginDTO.username()));
        final String token = jwtService.generateToken(Map.of(), user);

        return new AuthenticationResponseDTO(token);
    }

}
