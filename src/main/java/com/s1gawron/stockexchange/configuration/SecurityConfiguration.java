package com.s1gawron.stockexchange.configuration;

import com.s1gawron.stockexchange.jwt.JwtConfig;
import com.s1gawron.stockexchange.jwt.JwtTokenVerifier;
import com.s1gawron.stockexchange.jwt.JwtUsernamePasswordAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.cors.CorsConfiguration;

import javax.sql.DataSource;
import java.util.List;

@Configuration
@EnableWebSecurity
@CrossOrigin
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static final String USER_AUTH_QUERY = "SELECT username, password, enabled from user WHERE username=?";

    private static final String USER_AUTHORITY_QUERY = "SELECT username, role from user WHERE username=?";

    private final DataSource dataSource;

    private final JwtConfig jwtConfig;

    private final String frontendUrl;

    public SecurityConfiguration(final DataSource dataSource, final JwtConfig jwtConfig, @Value("${frontend.url}") final String frontendUrl) {
        this.dataSource = dataSource;
        this.jwtConfig = jwtConfig;
        this.frontendUrl = frontendUrl;
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .cors().configurationSource(request -> getCorsConfiguration())
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .addFilter(new JwtUsernamePasswordAuthenticationFilter(authenticationManager(), jwtConfig))
            .addFilterAfter(new JwtTokenVerifier(jwtConfig), JwtUsernamePasswordAuthenticationFilter.class)
            .authorizeRequests()
            .antMatchers("/api/v2/user/wallet").authenticated()
            .antMatchers("/transaction/perform").authenticated()
            .anyRequest().permitAll();
    }

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth
            .jdbcAuthentication()
            .dataSource(dataSource)
            .usersByUsernameQuery(USER_AUTH_QUERY)
            .authoritiesByUsernameQuery(USER_AUTHORITY_QUERY)
            .passwordEncoder(new BCryptPasswordEncoder());
    }

    private CorsConfiguration getCorsConfiguration() {
        final CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        corsConfiguration.setAllowedOrigins(List.of(frontendUrl));
        corsConfiguration.setAllowedMethods(List.of("GET", "POST"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setExposedHeaders(List.of("Authorization"));
        return corsConfiguration;
    }
}