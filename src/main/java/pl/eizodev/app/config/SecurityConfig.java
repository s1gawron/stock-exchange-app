package pl.eizodev.app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.sql.DataSource;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    DataSource dataSource;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/mainView").authenticated()
                .antMatchers("/myWallet").authenticated()
                .antMatchers("/order").authenticated()
                .antMatchers("/process-order").authenticated()
                .anyRequest().permitAll()
                .and()
//                .requiresChannel()
//                .antMatchers("/user/register").requiresSecure()
//                .antMatchers("/user/login").requiresSecure()
//                .antMatchers("/user/add-user").requiresSecure()
                .formLogin()
                .loginPage("/user/login")
                .loginProcessingUrl("/perform_login")
                .defaultSuccessUrl("/mainView")
                .failureUrl("/user/login?error=true")
                .and()
                .logout()
                .logoutUrl("/perform_logout")
                .logoutSuccessUrl("/user/login")
                .deleteCookies("JSESSIONID");
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .jdbcAuthentication()
                .dataSource(dataSource)
                .usersByUsernameQuery("select username, password, true from users where username=?")
                .authoritiesByUsernameQuery("select username, role from users where username=?")
                .passwordEncoder(new BCryptPasswordEncoder());
    }
}