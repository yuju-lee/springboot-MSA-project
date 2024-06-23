package com.sparta.springproject.controller;

import com.sparta.springproject.dto.LoginRequestDTO;
import com.sparta.springproject.dto.LoginResponseDTO;
import com.sparta.springproject.dto.MemberDTO;
import com.sparta.springproject.jwt.AuthResponse;
import com.sparta.springproject.jwt.JwtTokenProvider;
import com.sparta.springproject.jwt.JwtUtil;
import com.sparta.springproject.model.MemberEntity;
import com.sparta.springproject.repository.MemberRepository;
import com.sparta.springproject.service.AuthService;
import com.sparta.springproject.service.MemberService;
import com.sparta.springproject.service.TokenService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final MemberService memberService;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request, HttpServletResponse response) {
//        Authentication authentication = authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
//        );
//
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//        MemberEntity member = (MemberEntity) authentication.getPrincipal();
//        String accessToken = jwtTokenProvider.createAccessToken(member.getEmail());
//        String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail());
        authService.login(request, response);
        return ResponseEntity.ok("Login successful");
//
//        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam("refreshToken") String refreshToken) {
            authService.logout(refreshToken);
        // Implement logout logic (e.g., add refresh token to blacklist in Redis)
        return ResponseEntity.ok("Logout successful");
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

}
