package com.afriland.dottel.utilisateurs.api;

import com.afriland.dottel.utilisateurs.model.enums.RoleEnum;

/**
 * Destinataire d'une notification, vu depuis un autre module.
 *
 * Ne porte que les deux champs reellement consommes par NotificationService
 * (email et role) : exposer l'entite Utilisateur recreerait le couplage que le
 * chantier MM cherche a casser (CLAUDE.md section 18 point 4).
 */
public record DestinataireNotificationDto(String email, RoleEnum role) {
}
