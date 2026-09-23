package com.exemplo.authservice.service;

import org.springframework.stereotype.Service;
import com.exemplo.authservice.dto.LoginResponse;
import com.exemplo.authservice.model.RefreshToken;
import com.exemplo.authservice.model.Usuario;
import com.exemplo.authservice.repository.RefreshTokenRepository;
import com.exemplo.authservice.repository.UsuarioRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RefreshTokenService {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokens;
    private final UsuarioRepository usuarios;
    private final JwtToken jwtToken;
    private final long refreshExpirationSeconds;

    private static String hash(String token) {
        if (token == null || token.isBlank()) {
            throw naoAutorizado();
        }

        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponivel", e);
        }
    }

    private static ResponseStatusException naoAutorizado() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Refresh token invalido ou expirado"
        );
    }

    public RefreshTokenService(
            RefreshTokenRepository refreshTokens,
            UsuarioRepository usuarios,
            JwtToken jwtToken,
            @Value("${jwt.refresh-expiration-seconds:604800}")
            long refreshExpirationSeconds
    ) {
        if (refreshExpirationSeconds <= 0) {
            throw new IllegalArgumentException(
                    "A validade do refresh token deve ser positiva"
            );
        }

        this.refreshTokens = refreshTokens;
        this.usuarios = usuarios;
        this.jwtToken = jwtToken;
        this.refreshExpirationSeconds = refreshExpirationSeconds;
    }

    @Transactional
    public LoginResponse emitir(Usuario usuario) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);

        String refreshToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        refreshTokens.save(new RefreshToken(
                hash(refreshToken),
                usuario.getId(),
                Instant.now().plusSeconds(refreshExpirationSeconds)
        ));

        return new LoginResponse(
                jwtToken.gerarTokenAcesso(usuario),
                refreshToken,
                "Bearer",
                jwtToken.getAccessExpirationSeconds()
        );
    }

    @Transactional
    public LoginResponse renovar(String token) {
        RefreshToken anterior = refreshTokens
                .findByTokenHash(hash(token))
                .orElseThrow(RefreshTokenService::naoAutorizado);

        if (!anterior.getExpiresAt().isAfter(Instant.now())) {
            throw naoAutorizado();
        }

        Usuario usuario = usuarios.findById(anterior.getUsuarioId())
                .orElseThrow(RefreshTokenService::naoAutorizado);

        // Invalida o refresh anterior antes de emitir um novo par.
        refreshTokens.delete(anterior);
        refreshTokens.flush();

        return emitir(usuario);
    }
}
