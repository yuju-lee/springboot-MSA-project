package com.sparta.springproject.jwt;

import com.sparta.springproject.model.MemberEntity;
import com.sparta.springproject.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private MemberService memberService;

    @Autowired
    public JwtFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;

    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = jwtTokenProvider.resolveToken(request);
            if (token != null && jwtTokenProvider.validateToken(token)) {
                String email = jwtTokenProvider.getEmailFromToken(token);
                Optional<MemberEntity> optionalMember = memberService.findByEmail(email); // MemberService에서 이메일로 Member 조회

                if (optionalMember.isPresent()) {
                    MemberEntity member = optionalMember.get();
                    Authentication auth = new UsernamePasswordAuthenticationToken(member, null, member.getAuthorities());
                    auth = addAuthenticationDetails(auth, request);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private Authentication addAuthenticationDetails(Authentication auth, HttpServletRequest request) {
        if (auth != null) {
            ((UsernamePasswordAuthenticationToken) auth).setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        }
        return auth;
    }
}
