package com.sparta.springproject.jwt;

import com.sparta.springproject.model.MemberEntity;
import com.sparta.springproject.repository.JpaMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final JpaMemberRepository jpaMemberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String loginId = authentication.getName();
        String password = (String) authentication.getCredentials();

        MemberEntity memberEntity = jpaMemberRepository.findByEmail(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + loginId));

        if (!passwordEncoder.matches(password, memberEntity.getPassword())) {
            throw new BadCredentialsException("Invalid Password");
        }

        return new CustomAuthenticationToken(memberEntity, null, memberEntity.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(CustomAuthenticationToken.class);
    }
}
