package com.spa.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expiration;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // ============================================
    // GENERAR TOKEN: incluye "role" y "authorities"
    // ============================================

    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role) // EJ: ROLE_ADMIN
                .claim("authorities", List.of(role)) // NECESARIO PARA FRONTEND
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ============================================
    // EXTRAER USERNAME
    // ============================================

    public String extractUsername(String token) {
        try {
            return getAllClaims(token).getSubject();
        } catch (JwtException e) {
            System.out.println("Error al extraer username: " + e.getMessage());
            return null;
        }
    }

    // ============================================
    // EXTRAER ROL
    // ============================================

    public String extractRole(String token) {
        try {
            return getAllClaims(token).get("role", String.class);
        } catch (JwtException e) {
            System.out.println("Error al extraer rol: " + e.getMessage());
            return "ROLE_CLIENTE";
        }
    }

    //  Verifica si el token es válido
    public boolean isTokenValid(String token, String username) {
        try {
            String extractedUsername = extractUsername(token);
            return (extractedUsername != null &&
                    extractedUsername.equals(username) &&
                    !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("⚠️ Token inválido o manipulado: " + e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        try {
            return getAllClaims(token).getExpiration().before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }

    private Claims getAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
