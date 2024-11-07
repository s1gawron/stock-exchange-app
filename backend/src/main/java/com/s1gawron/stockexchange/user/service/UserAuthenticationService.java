package com.s1gawron.stockexchange.user.service;

import com.s1gawron.stockexchange.security.JwtService;
import com.s1gawron.stockexchange.user.dto.AuthenticationResponseDTO;
import com.s1gawron.stockexchange.user.dto.UserLoginDTO;
import com.s1gawron.stockexchange.user.model.User;
import com.s1gawron.stockexchange.user.dao.UserDAO;
import com.s1gawron.stockexchange.user.dao.filter.UserFilterParam;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserAuthenticationService {

    private final UserDAO userDAO;

    private final AuthenticationManager authManager;

    private final JwtService jwtService;

    public UserAuthenticationService(final UserDAO userDAO, final AuthenticationManager authManager, final JwtService jwtService) {
        this.userDAO = userDAO;
        this.authManager = authManager;
        this.jwtService = jwtService;
    }

    public AuthenticationResponseDTO loginUser(final UserLoginDTO userLoginDTO) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(userLoginDTO.username(), userLoginDTO.password()));

        final UserFilterParam filterParam = UserFilterParam.createForUsername(userLoginDTO.username());
        final User user = userDAO.findByFilter(filterParam).orElseThrow(() -> new BadCredentialsException("Bad credentials"));
        final String token = jwtService.generateToken(Map.of(), user);

        return new AuthenticationResponseDTO(token);
    }

}
