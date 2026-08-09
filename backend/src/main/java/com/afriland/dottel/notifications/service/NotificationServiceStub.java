package com.afriland.dottel.notifications.service;

import com.afriland.dottel.utilisateurs.api.DestinataireNotificationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Implementation par DEFAUT (dottel.notifications.enabled absent ou false) :
 * journalisation uniquement, aucun envoi reel. Permet de developper et de
 * tester sans serveur mail, exigence explicite du Sprint MM.13.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "dottel.notifications.enabled", havingValue = "false", matchIfMissing = true)
class NotificationServiceStub implements NotificationService {

    // Meme esprit que le stub EHR - contrat clair, implementation temporaire
    // remplacable plus tard par un envoi reel.
    @Override
    public void notifier(DestinataireNotificationDto destinataire, String sujet, String message) {
        log.info("Notification a {} ({}) - sujet: {} - message: {}",
                destinataire.email(), destinataire.role(), sujet, message);
    }
}
