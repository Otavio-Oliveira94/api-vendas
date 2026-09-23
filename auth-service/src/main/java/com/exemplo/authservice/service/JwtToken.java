package com.exemplo.authservice.service;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.exemplo.authservice.model.Usuario;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;


@Service 
public class JwtToken {

    private final SecretKey secretKey;
    private final long accessExpirationSeconds;

    public JwtToken(
            @Value("${jwt.secret}") String segredo,
            @Value("${jwt.access-expiration-seconds:900}") long accessExpirationSeconds
    ) {
        if (accessExpirationSeconds <= 0) {
            throw new IllegalArgumentException(
                    "A validade do access token deve ser positiva"
            );
        }

        this.secretKey = Keys.hmacShaKeyFor(
                segredo.getBytes(StandardCharsets.UTF_8)
        );
        this.accessExpirationSeconds = accessExpirationSeconds;
    }

    public String gerarTokenAcesso(Usuario usuario) {
        Instant agora = Instant.now();

        return Jwts.builder()
                .issuer("api-vendas")
                .subject(usuario.getEmail())
                .id(UUID.randomUUID().toString())
                .claim("token_type", "access")
                .issuedAt(Date.from(agora))
                .expiration(Date.from(
                        agora.plusSeconds(accessExpirationSeconds)
                ))
                .signWith(secretKey)
                .compact();
    }

    public long getAccessExpirationSeconds() {
        return accessExpirationSeconds;
    }
    
}
