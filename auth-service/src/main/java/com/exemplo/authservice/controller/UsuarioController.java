package com.exemplo.authservice.controller;

import com.exemplo.authservice.dto.LoginRequest;
import com.exemplo.authservice.dto.UsuarioRequest;
import com.exemplo.authservice.model.Usuario;
import com.exemplo.authservice.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.exemplo.authservice.dto.LoginResponse;
import com.exemplo.authservice.dto.RefreshRequest;
import com.exemplo.authservice.service.RefreshTokenService;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService service;
    private final RefreshTokenService refreshTokenService;

    @PostMapping
    public ResponseEntity<Long> cadastrar(
            @Valid @RequestBody UsuarioRequest request
    ) {
        Usuario usuario = service.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usuario.getId());
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        Usuario usuario = service.autenticar(request);
        return ResponseEntity.ok(refreshTokenService.emitir(usuario));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @Valid @RequestBody RefreshRequest request
    ) {
        return ResponseEntity.ok(
                refreshTokenService.renovar(request.refreshToken())
        );
    }
}
