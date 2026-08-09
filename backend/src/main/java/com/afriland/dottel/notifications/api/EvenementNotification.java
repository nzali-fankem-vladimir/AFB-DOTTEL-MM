package com.afriland.dottel.notifications.api;

import com.afriland.dottel.utilisateurs.api.DestinataireNotificationDto;

/**
 * Evenement applicatif publie par un module consommateur (processus,
 * referentiel) pour declencher l'envoi d'une notification, ecoute par
 * NotificationEventListener APRES commit de la transaction appelante
 * (Sprint MM.13, decision N-1+N-2) -- voir notifications/package-info.java.
 */
public record EvenementNotification(DestinataireNotificationDto destinataire, String sujet, String message) {
}
