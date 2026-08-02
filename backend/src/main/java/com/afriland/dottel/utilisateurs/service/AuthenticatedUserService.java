package com.afriland.dottel.utilisateurs.service;

import com.afriland.dottel.utilisateurs.exception.UtilisateurConnecteIntrouvableException;
import com.afriland.dottel.utilisateurs.model.entity.Utilisateur;
import com.afriland.dottel.utilisateurs.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticatedUserService {

    private final UtilisateurRepository utilisateurRepository;

    public Utilisateur utilisateurCourant() {
        String matricule = SecurityContextHolder.getContext().getAuthentication().getName();

        return utilisateurRepository.findByMatricule(matricule)
                .orElseThrow(() -> new UtilisateurConnecteIntrouvableException(
                        "Aucun utilisateur en base pour le matricule authentifié : " + matricule));
    }
}