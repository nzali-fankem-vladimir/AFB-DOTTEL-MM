package com.afriland.dottel.utilisateurs.service;
import com.afriland.dottel.audit.service.AuditService;

import com.afriland.dottel.utilisateurs.exception.IdentifiantsInvalidesException;
import com.afriland.dottel.utilisateurs.exception.UtilisateurInactifException;
import com.afriland.dottel.utilisateurs.model.dto.auth.LoginRequestDto;
import com.afriland.dottel.utilisateurs.model.dto.auth.LoginResponseDto;
import com.afriland.dottel.utilisateurs.model.entity.Utilisateur;
import com.afriland.dottel.utilisateurs.repository.UtilisateurRepository;
import com.afriland.dottel.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuditService auditService;

    public LoginResponseDto authentifier(LoginRequestDto requeteLogin) {
        // Matricule inconnu : aucun utilisateur trouve, donc aucun id valide pour
        // audit_log.id_utilisateur (NOT NULL + FK vers utilisateurs.id) -- non trace.
        Utilisateur utilisateur = utilisateurRepository.findByMatricule(requeteLogin.getMatricule())
                .orElseThrow(() -> new IdentifiantsInvalidesException("Matricule ou mot de passe incorrect"));

        if (!passwordEncoder.matches(requeteLogin.getMotDePasse(), utilisateur.getMotDePasseHash())) {
            auditService.enregistrer(utilisateur.getId(), "CONNEXION_ECHOUEE", "utilisateurs", utilisateur.getId(),
                    null, Map.of("matriculeTente", requeteLogin.getMatricule(), "motifEchec", "MOT_DE_PASSE_INCORRECT"));
            throw new IdentifiantsInvalidesException("Matricule ou mot de passe incorrect");
        }

        if (!utilisateur.isActif()) {
            auditService.enregistrer(utilisateur.getId(), "CONNEXION_ECHOUEE", "utilisateurs", utilisateur.getId(),
                    null, Map.of("matriculeTente", requeteLogin.getMatricule(), "motifEchec", "COMPTE_INACTIF"));
            throw new UtilisateurInactifException("Ce compte utilisateur est désactivé");
        }

        String token = jwtUtil.genererToken(utilisateur);

        auditService.enregistrer(utilisateur.getId(), "CONNEXION", "utilisateurs", utilisateur.getId(), null, null);

        return LoginResponseDto.builder()
                .token(token)
                .matricule(utilisateur.getMatricule())
                .role(utilisateur.getRole().name())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .build();
    }
}