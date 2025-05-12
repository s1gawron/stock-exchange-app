package com.s1gawron.stockexchange.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.s1gawron.stockexchange.user.dao.UserDAO;
import com.s1gawron.stockexchange.user.dao.filter.UserFilterParam;
import com.s1gawron.stockexchange.utils.ObjectMapperFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;

@Configuration
@EnableScheduling
public class StockExchangeAppConfiguration {

    private final UserDAO userDAO;

    public StockExchangeAppConfiguration(final UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userDAO.findByFilter(UserFilterParam.createForUsername(username))
            .orElseThrow(() -> new BadCredentialsException("Bad credentials"));
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        final DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(final AuthenticationConfiguration authConfiguration) throws Exception {
        return authConfiguration.getAuthenticationManager();
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return ObjectMapperFactory.I.getMapper();
    }

}
