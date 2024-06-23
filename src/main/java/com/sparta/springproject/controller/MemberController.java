package com.sparta.springproject.controller;

import com.sparta.springproject.dto.MemberDTO;
import com.sparta.springproject.dto.UpdatePasswordDTO;
import com.sparta.springproject.jwt.JwtUtil;
import com.sparta.springproject.model.MemberEntity;
import com.sparta.springproject.repository.MemberRepository;
import com.sparta.springproject.service.AuthService;
import com.sparta.springproject.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import com.sparta.springproject.service.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class MemberController {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberService memberService;
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;

    @Autowired
    public MemberController(MemberRepository memberRepository, PasswordEncoder passwordEncoder, MemberService memberService, AuthService authService, JwtUtil jwtUtil, TokenService tokenService) {

        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;

        this.memberService = memberService;
        this.authService = authService;
        this.tokenService = tokenService;

    }


    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody MemberDTO memberDTO) {
        try {
            // signup으로 들어오는 모든 user는 기본 회원이라는 가정으로 권한 부여
            memberDTO.setRole("ROLE_USER");
            // 회원 정보 저장
            MemberEntity savedMemberEntity = memberService.registerUser(memberDTO);

            // 저장 성공 시 응답
            String welcomeMessage = "Welcome, " + savedMemberEntity.getEmail() + "!";
            return ResponseEntity.ok(welcomeMessage);
        } catch (IllegalArgumentException e) {
            // 예외 발생 시 BadRequest 응답
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update-password")
    public void updatePassword(@RequestHeader("Authorization") String accessToken, @RequestBody UpdatePasswordDTO updatePasswordDTO) {
        String token = accessToken.replace(JwtUtil.BEARER_PREFIX, "");
        String username = jwtUtil.getUserInfoFromToken(token).getSubject();

        MemberEntity memberEntity = memberRepository.findByEmail(username).orElseThrow(
                () -> new IllegalArgumentException("User not found!")
        );

        if (!passwordEncoder.matches(updatePasswordDTO.getCurrentPassword(), memberEntity.getPassword())) {
            throw new IllegalArgumentException("Invalid current password.");
        }

        memberEntity.setPassword(passwordEncoder.encode(updatePasswordDTO.getNewPassword()));
        memberRepository.save(memberEntity);
    }

}
