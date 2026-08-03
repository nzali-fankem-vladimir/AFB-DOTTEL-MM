package com.afriland.dottel.utilisateurs.service;
import com.afriland.dottel.audit.api.EvenementAudit;

import com.afriland.dottel.utilisateurs.exception.ActionAdminNonAutoriseeException;
import com.afriland.dottel.utilisateurs.exception.EmailUtilisateurDejaUtiliseException;
import com.afriland.dottel.utilisateurs.exception.MatriculeUtilisateurDejaUtiliseException;
import com.afriland.dottel.utilisateurs.exception.RoleInvalideException;
import com.afriland.dottel.utilisateurs.exception.UtilisateurIntrouvableException;
import com.afriland.dottel.utilisateurs.model.dto.utilisateur.CreerUtilisateurRequestDto;
import com.afriland.dottel.utilisateurs.model.dto.utilisateur.UtilisateurResponseDto;
import com.afriland.dottel.utilisateurs.model.entity.Utilisateur;
import com.afriland.dottel.utilisateurs.model.enums.RoleEnum;
import com.afriland.dottel.utilisateurs.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UtilisateurAdminService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    // Pas de pagination : volume interne limite a quelques dizaines
    // d'utilisateurs (decision actee dans le guide du Sprint 4bis.3),
    // contrairement a GET /beneficiaires ou GET /processus.
    @Transactional(readOnly = true)
    public List<UtilisateurResponseDto> rechercher(RoleEnum role, Boolean actif) {
        return utilisateurRepository.findAll().stream()
                .filter(utilisateur -> role == null || utilisateur.getRole() == role)
                .filter(utilisateur -> actif == null || utilisateur.isActif() == actif)
                .map(this::versDto)
                .toList();
    }

    @Transactional
    public UtilisateurResponseDto creer(CreerUtilisateurRequestDto requete, Long idCreateur) {
        utilisateurRepository.findByMatricule(requete.getMatricule())
                .ifPresent(utilisateur -> {
                    throw new MatriculeUtilisateurDejaUtiliseException(
                            "Le matricule " + requete.getMatricule() + " est déjà utilisé");
                });

        utilisateurRepository.findByEmail(requete.getEmail())
                .ifPresent(utilisateur -> {
                    throw new EmailUtilisateurDejaUtiliseException(
                            "L'email " + requete.getEmail() + " est déjà utilisé");
                });

        Utilisateur utilisateur = Utilisateur.builder()
                .matricule(requete.getMatricule())
                .nom(requete.getNom())
                .prenom(requete.getPrenom())
                .email(requete.getEmail())
                .role(requete.getRole())
                .motDePasseHash(passwordEncoder.encode(requete.getMotDePasse()))
                .actif(true)
                .dateCreation(LocalDateTime.now())
                .build();

        utilisateurRepository.save(utilisateur);

        // RG-09 : jamais le mot de passe, meme hache, dans le delta d'audit.
        Map<String, Object> apres = new LinkedHashMap<>();
        apres.put("matricule", utilisateur.getMatricule());
        apres.put("nom", utilisateur.getNom());
        apres.put("prenom", utilisateur.getPrenom());
        apres.put("email", utilisateur.getEmail());
        apres.put("role", utilisateur.getRole().name());
        apres.put("actif", utilisateur.isActif());

        eventPublisher.publishEvent(new EvenementAudit(idCreateur, "CREATION_UTILISATEUR", "utilisateurs",
                utilisateur.getId(), null, apres));

        return versDto(utilisateur);
    }

    // Garde-fous convenus le 23/07/2026 : un ADMIN ne peut ni se
    // desactiver lui-meme, ni s'auto-retrograder, et le dernier compte
    // ADMIN actif restant ne peut etre ni desactive ni retrograde, meme
    // par un autre ADMIN.
    @Transactional
    public UtilisateurResponseDto changerStatut(Long id, boolean actif, Long idAdminConnecte) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new UtilisateurIntrouvableException(
                        "Aucun utilisateur trouvé pour l'id " + id));

        boolean desactivation = utilisateur.isActif() && !actif;
        if (desactivation) {
            if (id.equals(idAdminConnecte)) {
                throw new ActionAdminNonAutoriseeException(
                        "Un administrateur ne peut pas désactiver son propre compte");
            }
            verifierPasDernierAdminActif(utilisateur);
        }

        Map<String, Object> avant = new LinkedHashMap<>();
        avant.put("actif", utilisateur.isActif());

        utilisateur.setActif(actif);
        utilisateurRepository.save(utilisateur);

        Map<String, Object> apres = new LinkedHashMap<>();
        apres.put("actif", utilisateur.isActif());

        eventPublisher.publishEvent(new EvenementAudit(idAdminConnecte, "CHANGEMENT_STATUT_UTILISATEUR", "utilisateurs",
                utilisateur.getId(), avant, apres));

        return versDto(utilisateur);
    }

    @Transactional
    public UtilisateurResponseDto changerRole(Long id, String roleBrut, Long idAdminConnecte) {
        RoleEnum nouveauRole = parserRole(roleBrut);

        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new UtilisateurIntrouvableException(
                        "Aucun utilisateur trouvé pour l'id " + id));

        boolean retrogradation = utilisateur.getRole() != nouveauRole;
        if (retrogradation) {
            if (id.equals(idAdminConnecte) && nouveauRole != RoleEnum.ADMIN) {
                throw new ActionAdminNonAutoriseeException(
                        "Un administrateur ne peut pas changer son propre rôle vers un rôle différent d'ADMIN");
            }
            if (utilisateur.getRole() == RoleEnum.ADMIN && nouveauRole != RoleEnum.ADMIN) {
                verifierPasDernierAdminActif(utilisateur);
            }
        }

        Map<String, Object> avant = new LinkedHashMap<>();
        avant.put("role", utilisateur.getRole().name());

        utilisateur.setRole(nouveauRole);
        utilisateurRepository.save(utilisateur);

        Map<String, Object> apres = new LinkedHashMap<>();
        apres.put("role", utilisateur.getRole().name());

        eventPublisher.publishEvent(new EvenementAudit(idAdminConnecte, "CHANGEMENT_ROLE_UTILISATEUR", "utilisateurs",
                utilisateur.getId(), avant, apres));

        return versDto(utilisateur);
    }

    private RoleEnum parserRole(String roleBrut) {
        try {
            return RoleEnum.valueOf(roleBrut);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new RoleInvalideException("Rôle invalide : " + roleBrut);
        }
    }

    private void verifierPasDernierAdminActif(Utilisateur utilisateur) {
        if (utilisateur.getRole() == RoleEnum.ADMIN && utilisateur.isActif()) {
            long adminsActifs = utilisateurRepository.findByRoleAndActifTrue(RoleEnum.ADMIN).size();
            if (adminsActifs <= 1) {
                throw new ActionAdminNonAutoriseeException(
                        "Impossible de désactiver ou de rétrograder le dernier compte ADMIN actif restant");
            }
        }
    }

    private UtilisateurResponseDto versDto(Utilisateur utilisateur) {
        return UtilisateurResponseDto.builder()
                .id(utilisateur.getId())
                .matricule(utilisateur.getMatricule())
                .nomPrenoms(utilisateur.getNom() + " " + utilisateur.getPrenom())
                .role(utilisateur.getRole())
                .actif(utilisateur.isActif())
                .build();
    }
}