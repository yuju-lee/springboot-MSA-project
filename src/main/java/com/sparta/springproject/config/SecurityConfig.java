package com.sparta.springproject.config;

import com.sparta.springproject.filter.JwtAuthenticationEntryPoint;
import com.sparta.springproject.handler.JwtAccessDeniedHandler;
import com.sparta.springproject.jwt.JwtFilter;
import com.sparta.springproject.jwt.JwtTokenProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    private final JwtTokenProvider tokenProvider;
    private final ApplicationContext applicationContext;

    public SecurityConfig(JwtTokenProvider tokenProvider, ApplicationContext applicationContext) {
        this.tokenProvider = tokenProvider;
        this.applicationContext = applicationContext;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtFilter jwtFilter = new JwtFilter(tokenProvider);
        jwtFilter.setApplicationContext(applicationContext);

        http
                .cors(withDefaults())
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(withDefaults())
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling
                                .authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                                .accessDeniedHandler(new JwtAccessDeniedHandler())
                )
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers("/", "/api/login", "/api/signup", "/api/products", "/api/products/**").permitAll()
                                .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);  // JwtFilter를 직접 인스턴스화하여 추가

        return http.build();
    }
}