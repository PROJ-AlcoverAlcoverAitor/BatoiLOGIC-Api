package com.batoilogic.api.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    // Blacklist en memoria
    private final Set<String> tokensBloqueados = ConcurrentHashMap.newKeySet();

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generarToken(Long userId, String email, String rol) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("rol", rol)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(getKey())
                .compact();
    }

    public Claims validarToken(String token) {
        if (tokensBloqueados.contains(token)) {
            throw new JwtException("Token revocado");
        }
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public void revocarToken(String token) {
        tokensBloqueados.add(token);
    }

    public Long getUserId(String token) {
        return Long.parseLong(validarToken(token).getSubject());
    }

    public String getRol(String token) {
        return validarToken(token).get("rol", String.class);
    }

    public String getEmail(String token) {
        return validarToken(token).get("email", String.class);
    }
}
