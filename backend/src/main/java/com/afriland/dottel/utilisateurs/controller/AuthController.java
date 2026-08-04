package com.afriland.dottel.utilisateurs.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Sprint MM.7 : POST /auth/login disparait (decision de portee P-2). La
// connexion est desormais une redirection directe du frontend vers Keycloak
// (Authorization Code + PKCE, decision F-2) -- ce controleur n'intervient
// plus dans l'emission du jeton, Keycloak est l'unique emetteur.
@RestController
@RequestMapping("/auth")
public class AuthController {

    // Le jeton JWT est stateless (aucune session HTTP, cf. CLAUDE.md section 17 point 5) :
    // il n'existe pas de liste noire de jetons à ce stade. La déconnexion consiste
    // uniquement à supprimer le jeton côté client ; cet endpoint ne fait qu'acter la demande.
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok().build();
    }
}