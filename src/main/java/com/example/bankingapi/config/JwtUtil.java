package com.example.bankingapi.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {
    private final Key key;
    private final long EXPIRATION_TIME = 86400000;

    public JwtUtil(Dotenv dotenv) {
        String secretKey = dotenv.get("SECRET_KEY");
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // generate token with both email and accountNumber
    public String generateToken(String email, String accountNumber) {
        return Jwts.builder()
                .setSubject(email)
                .addClaims(Map.of("accountNumber", accountNumber))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractAccountNumber(String token) {
        return parseClaims(token).get("accountNumber", String.class);
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
