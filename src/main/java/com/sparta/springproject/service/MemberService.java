package com.sparta.springproject.service;

import com.sparta.springproject.dto.LoginRequestDTO;
import com.sparta.springproject.dto.MemberDTO;
import com.sparta.springproject.jwt.JwtUtil;
import com.sparta.springproject.model.Member;
import com.sparta.springproject.repository.MemberRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private final TokenBlacklistService tokenBlacklistService;



    @Autowired
    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService) {

        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    public Member registerUser(MemberDTO memberDTO) {
        if (memberDTO.getEmail() == null || memberDTO.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        if (memberDTO.getUserName() == null || memberDTO.getUserName().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        if (memberDTO.getPassword() == null || memberDTO.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        if (memberRepository.findByEmail(memberDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        Member member = new Member();
        member.setEmail(memberDTO.getEmail());
        member.setUserName(memberDTO.getUserName());
        member.setPassword(passwordEncoder.encode(memberDTO.getPassword()));

        try {
            return memberRepository.save(member);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Values cannot be null");
        }
    }

    public void login(LoginRequestDTO requestDto, HttpServletResponse res) {
        String email = requestDto.getEmail();
        String password = requestDto.getPassword();

        // 사용자 확인
        Member member = memberRepository.findByEmail(email).orElseThrow(
                () -> new IllegalArgumentException("Not registered - Please try again")
        );

        // 비밀번호 확인
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new IllegalArgumentException("Invalid password.");
        }

        // JWT 생성 및 쿠키에 저장 후 Response 객체에 추가
        String token = jwtUtil.createToken(member.getEmail(), member.getRole());
        jwtUtil.addJwtToCookie(token, res);
    }


    public ResponseEntity<String> logout(String token) {
        // 토큰을 블랙리스트에 추가
        tokenBlacklistService.addToBlacklist(token);
        // 로그에 블랙리스트에 추가된 토큰을 기록
        System.out.println("Blacklisted token: " + token);

        // 응답으로 블랙리스트에 추가된 토큰 반환
        return ResponseEntity.ok("Logout successful! Blacklisted token: " + token);
    }

}
