package com.afriland.dottel.controller;

import com.afriland.dottel.model.dto.auth.LoginRequestDto;
import com.afriland.dottel.model.dto.auth.LoginResponseDto;
import com.afriland.dottel.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto requeteLogin) {
        return ResponseEntity.ok(authService.authentifier(requeteLogin));
    }

    // Le jeton JWT est stateless (aucune session HTTP, cf. CLAUDE.md section 17 point 5) :
    // il n'existe pas de liste noire de jetons à ce stade. La déconnexion consiste
    // uniquement à supprimer le jeton côté client ; cet endpoint ne fait qu'acter la demande.
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok().build();
    }
}