package com.afriland.dottel.utilisateurs.api;

import com.afriland.dottel.utilisateurs.exception.UtilisateurConnecteIntrouvableException;
import com.afriland.dottel.utilisateurs.model.entity.Utilisateur;
import com.afriland.dottel.utilisateurs.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticatedUserService {

    private final UtilisateurRepository utilisateurRepository;

    // Decision I-2 (Sprint MM.7, question 3) : l'identite portee par le jeton
    // Keycloak est resolue par email, plus par matricule. Le claim "sub" d'un
    // jeton Keycloak est l'identifiant technique interne du realm (UUID), pas
    // le matricule DOTTEL -- utiliser getName() comme avant l'emetteur local
    // resoudrait donc systematiquement dans le vide.
    public Utilisateur utilisateurCourant() {
        JwtAuthenticationToken authentification =
                (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        String email = authentification.getToken().getClaimAsString("email");

        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UtilisateurConnecteIntrouvableException(
                        "Aucun utilisateur en base pour l'email authentifié : " + email));
    }
}