package com.sparta.springproject.filter;

import com.sparta.springproject.Util.JwtUtil;
import com.sparta.springproject.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService) {
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(JwtUtil.AUTHORIZATION_HEADER);

        if (StringUtils.hasText(header) && header.startsWith(JwtUtil.BEARER_PREFIX)) {
            String token = header.substring(JwtUtil.BEARER_PREFIX.length());
            if (jwtUtil.validateToken(token) && tokenBlacklistService.isBlacklisted(token)) {
                SecurityContextHolder.getContext().setAuthentication(jwtUtil.getAuthentication(token));
            } else {
                logger.warn("Invalid or blacklisted JWT token");
            }
        }

        chain.doFilter(request, response);
    }

}
