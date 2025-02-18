package com.s1gawron.stockexchange.user.service;

import com.s1gawron.stockexchange.security.JwtService;
import com.s1gawron.stockexchange.user.dto.AuthenticationResponseDTO;
import com.s1gawron.stockexchange.user.dto.UserLoginDTO;
import com.s1gawron.stockexchange.user.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserAuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(UserAuthenticationService.class);

    private final AuthenticationManager authManager;

    private final JwtService jwtService;

    public UserAuthenticationService(final AuthenticationManager authManager, final JwtService jwtService) {
        this.authManager = authManager;
        this.jwtService = jwtService;
    }

    public AuthenticationResponseDTO loginUser(final UserLoginDTO userLoginDTO) {
        final Authentication authenticate = authManager.authenticate(new UsernamePasswordAuthenticationToken(userLoginDTO.username(), userLoginDTO.password()));
        final User user = (User) authenticate.getPrincipal();
        final String token = jwtService.generateToken(Map.of(), user);

        log.info("User#{} logged in successfully", user.getId());

        return new AuthenticationResponseDTO(token);
    }

}
