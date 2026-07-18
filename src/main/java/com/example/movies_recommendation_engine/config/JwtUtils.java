package com.example.movies_recommendation_engine.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtils {

    private final Key jwtSecret;
    long jwtExpirationMs = 86400000;
    public JwtUtils(@Value("${jwt.secrets}") String secret){
        this.jwtSecret= Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret)
        );
    }
    public String generateToken(String email, String role){
        return Jwts.builder()
                .setSubject(email)
                .addClaims(Map.of("role", role))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() +jwtExpirationMs))
                .signWith(jwtSecret)
                .compact();
    }

    private Claims getClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    public String getEmail(String token){
        try{
            Claims claims = getClaims(token);
            return claims.getSubject();
        } catch (RuntimeException ex) {
            throw new RuntimeException("Token is expired");
        }

    }
    public String getRole(String token){
        try{
            Claims claims = getClaims(token);
            return claims.get("role", String.class);
        } catch (RuntimeException ex) {
            throw new RuntimeException("Token is expired");
        }

    }
}
