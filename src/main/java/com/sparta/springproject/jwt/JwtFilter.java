package com.sparta.springproject.jwt;

import com.sparta.springproject.model.MemberEntity;
import com.sparta.springproject.service.MemberService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

public class JwtFilter extends OncePerRequestFilter implements ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);
    private final JwtTokenProvider jwtTokenProvider;
    private ApplicationContext applicationContext;

    private static final String[] EXCLUDED_PATHS = {"/api/login", "/api/signup"};

    public JwtFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String requestPath = request.getRequestURI();
            AntPathMatcher pathMatcher = new AntPathMatcher();

            for (String excludedPath : EXCLUDED_PATHS) {
                if (pathMatcher.match(excludedPath, requestPath)) {
                    filterChain.doFilter(request, response);
                    return;
                }
            }

            String token = jwtTokenProvider.resolveToken(request);
            if (token != null && jwtTokenProvider.validateToken(token)) {
                String email = jwtTokenProvider.getEmailFromToken(token);
                if (email != null) {
                    MemberService memberService = applicationContext.getBean(MemberService.class);
                    Optional<MemberEntity> optionalMember = memberService.findByEmail(email);
                    if (optionalMember.isPresent()) {
                        MemberEntity member = optionalMember.get();
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(email, null, member.getAuthorities());
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    } else {
                        logger.error("Member not found for email: {}", email);
                    }
                } else {
                    logger.error("Failed to extract email from token");
                }
            } else {
                logger.error("Invalid or expired token");
            }
        } catch (Exception e) {
            logger.error("Authentication error: ", e);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}