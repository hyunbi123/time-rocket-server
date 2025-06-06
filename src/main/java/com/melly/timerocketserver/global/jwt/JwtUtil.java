package com.melly.timerocketserver.global.jwt;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
    private SecretKey secretKey;

    public JwtUtil(@Value("${spring.jwt.secret}")String secret) {
        // String 타입의 secret 을 객체변수(secretKey) 로 암호화
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    public String getCategory(String token){
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("category", String.class);
    }

    public String getUsername(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("username", String.class);
    }

    public String getRole(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    public Boolean isExpired(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    }

    // Jwt 생성
    public String createJwt(String category, String username, String role, Long expiredMs){
        return Jwts.builder()
                .claim("category",category)
                .claim("username",username)
                .claim("role",role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    // "토큰이 유효한지"만 빠르게 검사
    public boolean validate(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);  // 서명 검증 및 만료 검사 포함
            return true;  // 정상 검증됨
        } catch (io.jsonwebtoken.security.SecurityException | io.jsonwebtoken.MalformedJwtException e) {
            // 서명 오류, 토큰 변조 등
            return false;
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // 토큰 만료
            return false;
        } catch (Exception e) {
            // 기타 오류
            return false;
        }
    }
}
