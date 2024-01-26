package com.s1gawron.stockexchange.user.service;

import com.s1gawron.stockexchange.security.JwtService;
import com.s1gawron.stockexchange.shared.helper.UserCreatorHelper;
import com.s1gawron.stockexchange.user.dto.AuthenticationResponseDTO;
import com.s1gawron.stockexchange.user.dto.UserLoginDTO;
import com.s1gawron.stockexchange.user.exception.UserNotFoundException;
import com.s1gawron.stockexchange.user.model.User;
import com.s1gawron.stockexchange.user.repository.UserDAO;
import com.s1gawron.stockexchange.user.repository.filter.UserFilterParam;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserAuthenticationServiceTest {

    private static final String CUSTOMER_EMAIL = "customer@test.pl";

    private static final String PASSWORD = "Start00!";

    private static final String JWT_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjdXN0b21lckB0ZXN0LnBsIiwiaWF0IjoxNjg3NTkyNDg4LCJleHAiOjE2ODc1OTYwODh9.mnWvLAAflDV6PthsNXwgco-DP9D7pMSvV78y5gRraUA";

    private UserDAO userDAOMock;

    private JwtService jwtServiceMock;

    private UserAuthenticationService userAuthenticationService;

    @BeforeEach
    void setUp() {
        final AuthenticationManager authenticationManagerMock = Mockito.mock(AuthenticationManager.class);
        userDAOMock = Mockito.mock(UserDAO.class);
        jwtServiceMock = Mockito.mock(JwtService.class);
        userAuthenticationService = new UserAuthenticationService(userDAOMock, authenticationManagerMock, jwtServiceMock);
    }

    @Test
    void shouldLoginUser() {
        final User user = UserCreatorHelper.I.createUser();
        final UserLoginDTO userLoginDTO = new UserLoginDTO(CUSTOMER_EMAIL, PASSWORD);

        final UserFilterParam usernameFilter = UserFilterParam.createForUsername(userLoginDTO.username());
        Mockito.when(userDAOMock.findByFilter(usernameFilter)).thenReturn(Optional.of(user));
        Mockito.when(jwtServiceMock.generateToken(Map.of(), user)).thenReturn(JWT_TOKEN);

        final AuthenticationResponseDTO result = userAuthenticationService.loginUser(userLoginDTO);

        assertNotNull(result);
        assertTrue(isStringNotEmpty(result.token()));
        assertEquals(JWT_TOKEN, result.token());
    }

    @Test
    void shouldNotLoginUser() {
        final UserLoginDTO userLoginDTO = new UserLoginDTO(CUSTOMER_EMAIL, PASSWORD);

        assertThrows(UserNotFoundException.class, () -> userAuthenticationService.loginUser(userLoginDTO), "User#testUser could not be found!");
    }

    private boolean isStringNotEmpty(final String s) {
        final String trimmedString = s != null ? s.trim() : "";
        return !trimmedString.isEmpty();
    }

}