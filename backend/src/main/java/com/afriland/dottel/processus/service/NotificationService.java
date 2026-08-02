package com.afriland.dottel.processus.service;

import com.afriland.dottel.utilisateurs.api.DestinataireNotificationDto;

public interface NotificationService {

    // Sprint MM.2 : le destinataire est un DTO de l'API du module utilisateurs,
    // plus l'entite JPA Utilisateur. Le module processus cesse ainsi de dependre
    // du modele persistant d'un autre module pour notifier.
    void notifier(DestinataireNotificationDto destinataire, String sujet, String message);
}
