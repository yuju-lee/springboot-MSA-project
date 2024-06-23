package com.sparta.springproject.Util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtil {
    // Header KEY 값
    public static final String AUTHORIZATION_HEADER = "Authorization";
    // 사용자 권한 값의 KEY
    public static final String AUTHORIZATION_KEY = "auth";
    // Token 식별자
    public static final String BEARER_PREFIX = "Bearer ";

    @Value("${jwt_secret_key}") // Base64 Encode 한 SecretKey
    private String secretKey;

    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
    private final RedisUtil redisUtil;
    // 로그 설정
    public static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private final RedisTemplate<String, String> redisTemplate;

    @Autowired

    public JwtUtil(RedisUtil redisUtil, RedisTemplate<String, String> redisTemplate) {
        this.redisUtil = redisUtil;
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    // Access 토큰 생성
    public String createAccessToken(String username, String role) {
        Date date = new Date();

        // 토큰 만료시간
        long ACCESS_TOKEN_TIME = 60 * 60 * 1000L; // 60분
        return BEARER_PREFIX +
                Jwts.builder()
                        .setSubject(username) // 사용자 식별자값(ID)
                        .claim(AUTHORIZATION_KEY, role) // 사용자 권한
                        .setExpiration(new Date(date.getTime() + ACCESS_TOKEN_TIME)) // 만료 시간
                        .setIssuedAt(date) // 발급일
                        .signWith(key, signatureAlgorithm) // 암호화 알고리즘
                        .compact();
    }

    // Refresh 토큰 생성
    public String createRefreshToken() {
        Date date = new Date();

        long REFRESH_TOKEN_TIME = 14 * 24 * 60 * 60 * 1000L; // 14일
        return BEARER_PREFIX +
                Jwts.builder()
                        .setExpiration(new Date(date.getTime() + REFRESH_TOKEN_TIME)) // 만료 시간
                        .setIssuedAt(date) // 발급일
                        .signWith(key, signatureAlgorithm) // 암호화 알고리즘
                        .compact();
    }

    // 토큰 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("Invalid or expired JWT token.");
        }
        return false;
    }

    // 토큰에서 사용자 정보 가져오기
    public Claims getUserInfoFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    // JwtUtil 클래스 내에 추가
    public void addJwtToCookie(String token, HttpServletResponse res) {
        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8).replace("+", "%20");
        Cookie cookie = new Cookie(AUTHORIZATION_HEADER, encodedToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        res.addCookie(cookie);
    }

    // JWT 토큰에서 Authentication 객체 생성
    public Authentication getAuthentication(String token) {
        Claims claims = getUserInfoFromToken(token);
        String username = claims.getSubject();
        String role = (String) claims.get(AUTHORIZATION_KEY);
        Collection<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));

        return new UsernamePasswordAuthenticationToken(username, "", authorities);
    }

    // JWT 토큰에서 추출
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // Access Token을 블랙리스트에 추가하는 메서드
    public void addToBlacklist(String token) {
        long expirationTime = getExpiration(token).getTime() - System.currentTimeMillis();
        redisTemplate.opsForValue().set(token, "blacklisted", expirationTime, TimeUnit.MILLISECONDS);
    }

    // 토큰이 블랙리스트에 있는지 검사하는 메서드
    public boolean validateTokenConsideringBlacklist(String token) {
        return validateToken(token) && !isBlacklisted(token);
    }

    // 토큰이 블랙리스트에 있는지 확인하는 메서드
    public boolean isBlacklisted(String token) {
        String value = redisTemplate.opsForValue().get(token);
        return "blacklisted".equals(value);
    }

    // 토큰의 만료 시간 가져오기
    public Date getExpiration(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return claims.getExpiration();
    }


}
