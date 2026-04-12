package com.greenloop.auth.security;

import com.greenloop.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtTokenProvider {

    @Value("${jwt.secret:your-super-secret-key-that-must-be-at-least-256-bits-long-for-HS256}")
    private String jwtSecret;

    @Value("${jwt.expiration.access:3600000}")
    private long accessTokenExpirationMs;

    @Value("${jwt.expiration.refresh:604800000}")
    private long refreshTokenExpirationMs;

    public String generateAccessToken(User user) {
        return generateToken(user, accessTokenExpirationMs, "access");
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, refreshTokenExpirationMs, "refresh");
    }

    private String generateToken(User user, long expirationTime, String tokenType) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("name", user.getName())
                .claim("role", user.getRole().name())
                .claim("universityVerified", user.isUniversityVerified())
                .claim("type", tokenType)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }   

    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getUserIdFromToken(String token) {
        return getAllClaims(token).getSubject();
    }

    public String getRoleFromToken(String token) {
        return getAllClaims(token).get("role", String.class);
    }

    public String getEmailFromToken(String token) {
        return getAllClaims(token).get("email", String.class);
    }

    public String getNameFromToken(String token) {
        return getAllClaims(token).get("name", String.class);
    }

    public Boolean isUniversityVerifiedFromToken(String token) {
        return getAllClaims(token).get("universityVerified", Boolean.class);
    }

    public String getTokenTypeFromToken(String token) {
        return getAllClaims(token).get("type", String.class);
    }

    public Date getExpirationDateFromToken(String token) {
        return getAllClaims(token).getExpiration();
    }

    public Boolean isTokenExpired(String token) {
        try {
            return getExpirationDateFromToken(token).before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    private Claims getAllClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
