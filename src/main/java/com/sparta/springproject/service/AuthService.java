package com.sparta.springproject.service;

import com.sparta.springproject.dto.LoginRequestDTO;
import com.sparta.springproject.jwt.JwtUtil;
import com.sparta.springproject.model.Member;
import com.sparta.springproject.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class AuthService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    public void login(LoginRequestDTO requestDto, HttpServletResponse res) {
        String email = requestDto.getEmail();
        String password = requestDto.getPassword();

        Member member = memberRepository.findByEmail(email).orElseThrow(
                () -> new IllegalArgumentException("Not registered - Please try again")
        );

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password.");
        }

        String token = jwtUtil.createToken(member.getEmail(), member.getRole());
        jwtUtil.addJwtToCookie(token, res);
    }

    public ResponseEntity<String> logout(String token) {
        tokenBlacklistService.addToBlacklist(token);
        return ResponseEntity.ok("Logout successful! Token: " + token);
    }
}
