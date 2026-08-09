package com.afriland.dottel.notifications.service;

import com.afriland.dottel.utilisateurs.api.DestinataireNotificationDto;

/**
 * Frontiere du canal de notification externe.
 *
 * Deux implementations, sur le modele EhrIntegrationService /
 * EhrIntegrationServiceStub : NotificationServiceSmtp quand
 * dottel.notifications.enabled vaut true, NotificationServiceStub sinon
 * (defaut, developpement local sans serveur mail).
 *
 * Interne au module notifications depuis MM.13 : les consommateurs publient
 * un EvenementNotification plutot que d'appeler cette interface directement,
 * ce qui garantit l'envoi APRES commit (voir NotificationEventListener).
 */
public interface NotificationService {

    // Sprint MM.2 : le destinataire est un DTO de l'API du module utilisateurs,
    // plus l'entite JPA Utilisateur. Le module processus cesse ainsi de dependre
    // du modele persistant d'un autre module pour notifier.
    void notifier(DestinataireNotificationDto destinataire, String sujet, String message);
}
