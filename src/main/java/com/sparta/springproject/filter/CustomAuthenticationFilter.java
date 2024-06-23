package com.sparta.springproject.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.springproject.jwt.CustomAuthenticationToken;
import com.sparta.springproject.Util.JwtUtil;
import com.sparta.springproject.service.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;

import java.io.IOException;

public class CustomAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager, JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService) {
        super(new AntPathRequestMatcher("/api/login"));
        setAuthenticationManager(authenticationManager);
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException {

        // JWT 토큰이 있는지 확인
        String token = jwtUtil.resolveToken(request);
        if (token != null && jwtUtil.validateToken(token) && !tokenBlacklistService.isBlacklisted(token)) {
            // JWT 토큰이 유효하고 블랙리스트에 없는 경우
            SecurityContextHolder.getContext().setAuthentication(jwtUtil.getAuthentication(token));
            return SecurityContextHolder.getContext().getAuthentication();
        }

        // 해당 요청이 POST 인지 확인
        if (!isPost(request)) {
            throw new IllegalStateException("Authentication is not supported");
        }

        // POST일 경우 body 를 AccountDto(로그인 정보 DTO) 에 매핑
        AccountDto accountDto = objectMapper.readValue(request.getReader(), AccountDto.class);

        // ID, PASSWORD 가 있는지 확인
        if (!StringUtils.hasLength(accountDto.getEmail())
                || !StringUtils.hasLength(accountDto.getPassword())) {
            throw new IllegalArgumentException("username or password is empty");
        }

        // 처음에는 인증 되지 않은 토큰 생성
        CustomAuthenticationToken authRequest = new CustomAuthenticationToken(
                accountDto.getEmail(),
                accountDto.getPassword()
        );

        // Manager 에게 인증 처리
        return getAuthenticationManager().authenticate(authRequest);
    }

    private boolean isPost(HttpServletRequest request) {
        return "POST".equals(request.getMethod());
    }

    @Data
    public static class AccountDto {
        private String email;
        private String password;
    }
}
