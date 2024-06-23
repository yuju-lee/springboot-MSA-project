package com.sparta.springproject.controller;

import com.sparta.springproject.dto.LoginRequestDTO;
import com.sparta.springproject.Util.JwtUtil;
import com.sparta.springproject.service.AuthService;
import com.sparta.springproject.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request, HttpServletResponse response) {
        return authService.login(request, response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String accessToken) {
        authService.logout(accessToken);
        return ResponseEntity.ok("Logout successful");
    }

    @PostMapping("/refresh-token")
    public void refreshToken(@RequestHeader("Authorization") String accessToken, @RequestHeader("RefreshToken") String refreshToken, HttpServletResponse res) {
        String token = accessToken.replace(JwtUtil.BEARER_PREFIX, "");
        String username = jwtUtil.getUserInfoFromToken(token).getSubject();

        if (!jwtUtil.validateToken(token) && tokenService.getRefreshToken(username).equals(refreshToken) && jwtUtil.validateToken(refreshToken)) {
            String newAccessToken = jwtUtil.createAccessToken(username, jwtUtil.getUserInfoFromToken(token).get(JwtUtil.AUTHORIZATION_KEY).toString());
            String newRefreshToken = jwtUtil.createRefreshToken();

            tokenService.storeRefreshToken(username, newRefreshToken);

            jwtUtil.addJwtToCookie(newAccessToken, res);
            res.addHeader("RefreshToken", newRefreshToken);
        } else {
            throw new IllegalArgumentException("Invalid refresh token or access token.");
        }
    }

}
