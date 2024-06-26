package com.sparta.springproject.service;

import com.sparta.springproject.dto.LoginRequestDTO;
import com.sparta.springproject.dto.LoginResponseDTO;
import com.sparta.springproject.Util.JwtUtil;
import com.sparta.springproject.model.MemberEntity;
import com.sparta.springproject.repository.JpaMemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class AuthService {

    private final JpaMemberRepository jpaMemberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);


    @Autowired
    public AuthService(JpaMemberRepository jpaMemberRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, TokenService tokenService) {
        this.jpaMemberRepository = jpaMemberRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.tokenService = tokenService;
    }

    public ResponseEntity<LoginResponseDTO>login(LoginRequestDTO requestDto, HttpServletResponse res) {
        String email = requestDto.getEmail();
        String password = requestDto.getPassword();
        log.info(email);

        try {
            // 이메일로 회원 조회
            MemberEntity memberEntity = jpaMemberRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Not registered - Please try again"));

            // 비밀번호 검증
            if (!passwordEncoder.matches(password, memberEntity.getPassword())) {
                throw new IllegalArgumentException("Invalid password.");
            }

            // Access Token 및 Refresh Token 생성
            String accessToken = jwtUtil.createAccessToken(memberEntity.getEmail(), memberEntity.getRole());
            String refreshToken = jwtUtil.createRefreshToken();

            // Refresh Token 저장 (Redis에 저장되어야 함)
            tokenService.storeRefreshToken(memberEntity.getEmail(), refreshToken);


            // Refresh Token을 응답 헤더에 추가
            res.addHeader("RefreshToken", refreshToken);
            res.addHeader("AccessToken", accessToken);


            // 로그인 성공 응답에 발급받은 토큰들 추가
            LoginResponseDTO responseDTO = new LoginResponseDTO(accessToken, refreshToken, memberEntity.getEmail());
            responseDTO.setAccessToken(accessToken);
            responseDTO.setRefreshToken(refreshToken);
            responseDTO.setMessage("Login successful! Welcome, " + memberEntity.getEmail() + "!");

            return ResponseEntity.ok(responseDTO);
        } catch (IllegalArgumentException e) {
            log.error("Error during login: {}", e.getMessage());
            LoginResponseDTO responseDTO = new LoginResponseDTO(null, null, e.getMessage());
            responseDTO.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDTO);
        }
    }

    public void logout(String accessToken) {
        String token = accessToken.replace(JwtUtil.BEARER_PREFIX, "");
        if (jwtUtil.validateTokenConsideringBlacklist(token)) {
            tokenService.addToBlacklist(accessToken);
        } else {
            throw new IllegalArgumentException("Invalid or expired JWT token.");
        }
    }
}
