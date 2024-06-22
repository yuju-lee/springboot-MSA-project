package com.sparta.springproject.controller;

import com.sparta.springproject.dto.LoginRequestDTO;
import com.sparta.springproject.dto.LoginResponseDTO;
import com.sparta.springproject.dto.MemberDTO;
import com.sparta.springproject.dto.UpdatePasswordDTO;
import com.sparta.springproject.jwt.JwtUtil;
import com.sparta.springproject.model.MemberEntity;
import com.sparta.springproject.repository.MemberRepository;
import com.sparta.springproject.service.AuthService;
import com.sparta.springproject.service.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.sparta.springproject.service.TokenService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO requestDto, HttpServletResponse res) {
        return authService.login(requestDto, res);
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader("Authorization") String accessToken) {
        authService.logout(accessToken);
    }

    @PostMapping("/refresh-token")
    public void refreshToken(@RequestHeader("Authorization") String accessToken, @RequestHeader("X-Refresh-Token") String refreshToken, HttpServletResponse res) {
        String token = accessToken.replace(JwtUtil.BEARER_PREFIX, "");
        String username = jwtUtil.getUserInfoFromToken(token).getSubject();

        if (!jwtUtil.validateToken(token) && tokenService.getRefreshToken(username).equals(refreshToken) && jwtUtil.validateToken(refreshToken)) {
            String newAccessToken = jwtUtil.createAccessToken(username, jwtUtil.getUserInfoFromToken(token).get(JwtUtil.AUTHORIZATION_KEY).toString());
            String newRefreshToken = jwtUtil.createRefreshToken();

            tokenService.storeRefreshToken(username, newRefreshToken);

            jwtUtil.addJwtToCookie(newAccessToken, res);
            res.addHeader("X-Refresh-Token", newRefreshToken);
        } else {
            throw new IllegalArgumentException("Invalid refresh token or access token.");
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

    @GetMapping("/mypage")
    public ResponseEntity<String> myPage(@AuthenticationPrincipal MemberEntity memberEntity) {
        // 여기서 userDetails는 Spring Security에서 인증된 사용자 정보를 제공합니다.
        // 실제로는 UserDetails를 구현한 객체인 MemberEntity 혹은 CustomUserDetails 등을 사용해야 합니다.

        // 예시로 UserDetails를 사용하는 경우
        String username = memberEntity.getUserName();
        String message = "Welcome to your MyPage, " + username + "!";

        return ResponseEntity.ok(message);
    }
}
