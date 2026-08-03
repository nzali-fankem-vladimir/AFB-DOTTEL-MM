package com.afriland.dottel.processus.service;

import com.afriland.dottel.utilisateurs.api.DestinataireNotificationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class NotificationServiceStub implements NotificationService {

    // Pas d'integration email/SMS prevue a ce stade : journalisation uniquement.
    // Meme esprit que le stub EHR - contrat clair, implementation temporaire
    // remplacable plus tard par un envoi reel.
    @Override
    public void notifier(DestinataireNotificationDto destinataire, String sujet, String message) {
        log.info("Notification a {} ({}) - sujet: {} - message: {}",
                destinataire.email(), destinataire.role(), sujet, message);
    }
}
