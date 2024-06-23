package com.sparta.springproject.service;

import com.sparta.springproject.dto.LoginRequestDTO;
import com.sparta.springproject.dto.LoginResponseDTO;
import com.sparta.springproject.jwt.JwtUtil;
import com.sparta.springproject.model.MemberEntity;
import com.sparta.springproject.repository.MemberRepository;
import org.apache.commons.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;
    private final TokenService tokenService;
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);


    @Autowired
    public AuthService(MemberRepository memberRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService, TokenService tokenService) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
        this.tokenService = tokenService;
    }

    public ResponseEntity<LoginResponseDTO> login(LoginRequestDTO requestDto, HttpServletResponse res) {
        String email = requestDto.getEmail();
        String password = requestDto.getPassword();

        // 이메일로 회원 조회
        MemberEntity memberEntity = memberRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}");
                    return new IllegalArgumentException("Not registered - Please try again");
                });

        // 비밀번호 검증
        if (!passwordEncoder.matches(password, memberEntity.getPassword())) {
            log.error("Invalid password for user: {}");
            throw new IllegalArgumentException("Invalid password.");
        }

        // Access Token 및 Refresh Token 생성
        String accessToken = jwtUtil.createAccessToken(memberEntity.getEmail(), memberEntity.getRole());
        String refreshToken = jwtUtil.createRefreshToken();

        // Refresh Token 저장 (Redis에 저장되어야 함)
        tokenService.storeRefreshToken(memberEntity.getEmail(), refreshToken);

        // 엑세스 토큰을 쿠키에 추가
        jwtUtil.addJwtToCookie(accessToken, res);
        jwtUtil.addJwtToCookie(refreshToken, res);

        // Refresh Token을 응답 헤더에 추가
        res.addHeader("X-Refresh-Token", refreshToken);

        // 로그인 성공 응답에 발급받은 토큰들 추가
        LoginResponseDTO responseDTO = new LoginResponseDTO(accessToken, refreshToken, memberEntity.getEmail());
        responseDTO.setAccessToken(accessToken);
        responseDTO.setRefreshToken(refreshToken);
        responseDTO.setMessage("Login successful! Welcome, " + memberEntity.getEmail() + "!");

        return ResponseEntity.ok(responseDTO);
    }

    public void logout(String accessToken) {
        String token = accessToken.replace(JwtUtil.BEARER_PREFIX, "");
        tokenBlacklistService.addToBlacklist(token);

        String username = jwtUtil.getUserInfoFromToken(token).getSubject();
        tokenService.deleteRefreshToken(username);
    }
}
