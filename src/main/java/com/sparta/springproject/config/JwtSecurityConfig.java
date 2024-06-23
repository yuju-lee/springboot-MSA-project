package com.sparta.springproject.config;

import com.sparta.springproject.jwt.JwtFilter;
import com.sparta.springproject.jwt.JwtTokenProvider;
import com.sparta.springproject.service.MemberService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class JwtSecurityConfig {

    private final JwtTokenProvider tokenProvider;
    private final MemberService memberService;

    public JwtSecurityConfig(JwtTokenProvider tokenProvider, MemberService memberService) {
        this.tokenProvider = tokenProvider;
        this.memberService = memberService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(new JwtFilter(tokenProvider, memberService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}