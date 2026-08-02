package com.afriland.dottel.utilisateurs.api;

import com.afriland.dottel.utilisateurs.model.enums.RoleEnum;

import java.util.List;

/**
 * API publique du module utilisateurs, destinee aux autres modules.
 *
 * Introduite au Sprint MM.2 pour que ProcessusMensuelService cesse d'injecter
 * UtilisateurRepository. Aucune methode ne retourne l'entite JPA Utilisateur.
 */
public interface UtilisateurApi {

    /**
     * Destinataires actifs portant le role demande, pour notification.
     *
     * Retourne une liste vide si personne ne correspond -- jamais d'exception :
     * une validation ARH reste valide meme si aucun CRH actif n'existe.
     */
    List<DestinataireNotificationDto> destinatairesParRole(RoleEnum role);

    /**
     * Destinataire identifie par son id.
     *
     * Leve UtilisateurIntrouvableException si l'id ne correspond a aucun
     * utilisateur -- le 404 attendu par le contrat API est ainsi preserve.
     */
    DestinataireNotificationDto destinataireParId(Long idUtilisateur);
}
