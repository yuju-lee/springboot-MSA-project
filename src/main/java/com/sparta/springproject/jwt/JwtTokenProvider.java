package com.sparta.springproject.jwt;

import com.sparta.springproject.service.AuthService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenProvider {


    @Value("${jwt_secret_key}")
    private String secretKey;

    private long accessTokenValidityInMilliseconds;
    private long refreshTokenValidityInMilliseconds;
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @PostConstruct
    protected void init() {
        this.accessTokenValidityInMilliseconds = 1000 * 60 * 60 * 12; // 12시간
        this.refreshTokenValidityInMilliseconds = 1000 * 60 * 60 * 24 * 7; // 7일
    }

    public String createAccessToken(String email) {
        return createToken(email, accessTokenValidityInMilliseconds);
    }

    public String createRefreshToken(String email) {
        return createToken(email, refreshTokenValidityInMilliseconds);
    }

    private String createToken(String email, long validityTime) {
        Claims claims = Jwts.claims().setSubject(email);

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityTime);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid or expired JWT token", e);
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Failed to get email from token", e);
            return null;
        }
    }

}
