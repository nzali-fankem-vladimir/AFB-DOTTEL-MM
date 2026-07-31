package com.afriland.dottel.service;

import com.afriland.dottel.exception.UtilisateurConnecteIntrouvableException;
import com.afriland.dottel.model.entity.Utilisateur;
import com.afriland.dottel.repository.UtilisateurRepository;
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