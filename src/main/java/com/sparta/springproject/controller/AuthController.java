package com.sparta.springproject.controller;

import com.sparta.springproject.dto.LoginRequestDTO;
import com.sparta.springproject.dto.LoginResponseDTO;
import com.sparta.springproject.dto.MemberDTO;
import com.sparta.springproject.jwt.AuthResponse;
import com.sparta.springproject.jwt.JwtTokenProvider;
import com.sparta.springproject.model.MemberEntity;
import com.sparta.springproject.service.AuthService;
import com.sparta.springproject.service.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final MemberService memberService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        MemberEntity member = (MemberEntity) authentication.getPrincipal();
        String accessToken = jwtTokenProvider.createAccessToken(member.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail());

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
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

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam("refreshToken") String refreshToken) {
            authService.logout(refreshToken);
        // Implement logout logic (e.g., add refresh token to blacklist in Redis)
        return ResponseEntity.ok("Logout successful");
    }

}
