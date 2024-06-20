package com.sparta.springproject.service;

import com.sparta.springproject.dto.LoginRequestDTO;
import com.sparta.springproject.dto.MemberDTO;
import com.sparta.springproject.jwt.JwtUtil;
import com.sparta.springproject.model.Member;
import com.sparta.springproject.repository.MemberRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


    @Autowired
    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public Member registerUser(MemberDTO memberDTO) {
        if (memberRepository.findByEmail(memberDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already in use.");
        }

        Member member = new Member();
        member.setEmail(memberDTO.getEmail());
        member.setUserName(passwordEncoder.encode(memberDTO.getUserName()));
        member.setPassword(passwordEncoder.encode(memberDTO.getPassword()));
        return memberRepository.save(member);
    }

    public ResponseEntity<String> login(LoginRequestDTO requestDto, HttpServletResponse res) {
        String email = requestDto.getEmail();
        String password = requestDto.getPassword();

        // 사용자 확인
        Member member = memberRepository.findByEmail(email).orElseThrow(
                () -> new IllegalArgumentException("Not registerd - Please try again")
        );

        // 비밀번호 확인
        if (!passwordEncoder.matches(password, member.getPassword())) {
            return ResponseEntity.badRequest().body("Invalid username or password.");
        }

        // JWT 생성 및 쿠키에 저장 후 Response 객체에 추가
        String token = jwtUtil.createToken(member.getEmail(), member.getRole());
        jwtUtil.addJwtToCookie(token, res);

        return ResponseEntity.ok("Login successful!");
    }
}
