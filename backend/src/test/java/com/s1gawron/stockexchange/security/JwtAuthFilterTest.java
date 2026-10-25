package com.s1gawron.stockexchange.security;

import com.s1gawron.stockexchange.shared.helper.UserCreatorHelper;
import com.s1gawron.stockexchange.user.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;

class JwtAuthFilterTest {

    private static final String TOKEN = "token";

    private JwtService jwtServiceMock;

    private JwtAuthFilter underTest;

    private MockHttpServletRequest request;

    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtServiceMock = Mockito.mock(JwtService.class);
        underTest = new JwtAuthFilter(jwtServiceMock);
        request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + TOKEN);
        filterChain = new MockFilterChain();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateUserWhenTokenIsValid() throws Exception {
        final User user = UserCreatorHelper.I.createUser();
        Mockito.when(jwtServiceMock.validateToken(TOKEN)).thenReturn(new JwtValidationResult(true, user));

        underTest.doFilter(request, new MockHttpServletResponse(), filterChain);

        assertNotNull(filterChain.getRequest());
        assertEquals(user, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    void shouldNotAuthenticateUserWhenTokenIsNotValid() throws Exception {
        final User user = UserCreatorHelper.I.createUser();
        Mockito.when(jwtServiceMock.validateToken(TOKEN)).thenReturn(new JwtValidationResult(false, user));

        underTest.doFilter(request, new MockHttpServletResponse(), filterChain);

        assertNotNull(filterChain.getRequest());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldContinueFilterChainWithoutAuthenticationWhenUserFromTokenDoesNotExist() throws Exception {
        Mockito.when(jwtServiceMock.validateToken(TOKEN)).thenThrow(new BadCredentialsException("Bad credentials"));

        underTest.doFilter(request, new MockHttpServletResponse(), filterChain);

        assertNotNull(filterChain.getRequest());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

}
