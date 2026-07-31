package com.afriland.dottel.security;

import com.afriland.dottel.model.entity.Utilisateur;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private static final long EXPIRATION_MS = 8 * 60 * 60 * 1000;

    public static final MacAlgorithm ALGORITHME = Jwts.SIG.HS384;

    private final SecretKey secretKey;

    public JwtUtil(@Value("${dottel.security.jwt-secret}") String jwtSecret) {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String genererToken(Utilisateur utilisateur) {
        Date maintenant = new Date();
        Date expiration = new Date(maintenant.getTime() + EXPIRATION_MS);

        return Jwts.builder()
                .subject(utilisateur.getMatricule())
                .claim("role", utilisateur.getRole().name())
                .issuedAt(maintenant)
                .expiration(expiration)
                .signWith(secretKey, ALGORITHME)
                .compact();
    }
}