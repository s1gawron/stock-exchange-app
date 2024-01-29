package com.s1gawron.stockexchange.configuration;

import com.s1gawron.stockexchange.security.JwtAuthFilter;
import com.s1gawron.stockexchange.user.model.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final AuthenticationProvider authenticationProvider;

    private final JwtAuthFilter jwtAuthFilter;

    private final String frontendUrl;

    public SecurityConfiguration(final AuthenticationProvider authenticationProvider, final JwtAuthFilter jwtAuthFilter,
        @Value("${frontend.url}") final String frontendUrl) {
        this.authenticationProvider = authenticationProvider;
        this.jwtAuthFilter = jwtAuthFilter;
        this.frontendUrl = frontendUrl;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity httpSecurity) throws Exception {
        httpSecurity
            .csrf().disable()
            .cors().configurationSource(request -> getCorsConfiguration())
            .and()
            .authorizeHttpRequests()
            .requestMatchers("/api/public/**", "/api-docs/**", "swagger-ui/**").permitAll()
            .requestMatchers("/api/user/**", "/api/transaction/**").hasAuthority(UserRole.USER.name())
            .anyRequest().authenticated()
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
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