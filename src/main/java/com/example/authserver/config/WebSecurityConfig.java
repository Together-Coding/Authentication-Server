package com.example.authserver.config;

import com.example.authserver.security.filter.ApiCheckFilter;
import com.example.authserver.security.filter.ApiLoginFilter;
import com.example.authserver.security.handler.LoginFailHandler;
import com.example.authserver.security.service.AuthUserDetailsService;
import com.example.authserver.security.util.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private AuthUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.httpBasic().disable();
        http.formLogin().disable();
        http.csrf().disable();
        http.headers().frameOptions().sameOrigin();
        http.rememberMe().tokenValiditySeconds(60 * 60 * 24 * 7).userDetailsService(userDetailsService);
        http.addFilterBefore(apiCheckFilter(),
                UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(apiLoginFilter(),
                UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public JWTUtil jwtUtil() {
        return new JWTUtil();
    }

    private final String[] excludePaths =
            new String[]{
                    "/auth/signup", "/auth/signin", "/h2-console/**"};

    @Bean
    public ApiCheckFilter apiCheckFilter() {
        return new ApiCheckFilter("/**", excludePaths, jwtUtil());
    }


    @Bean
    public ApiLoginFilter apiLoginFilter() throws Exception {
        ApiLoginFilter apiLoginFilter = new ApiLoginFilter
                ("/auth/signin", jwtUtil());
        apiLoginFilter.setAuthenticationManager(authenticationManager());
        apiLoginFilter.setAuthenticationFailureHandler(new LoginFailHandler());

        return apiLoginFilter;
    }
}
