package com.afriland.dottel.service;

import com.afriland.dottel.model.entity.Utilisateur;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationServiceStub implements NotificationService {

    // Pas d'integration email/SMS prevue a ce stade : journalisation uniquement.
    // Meme esprit que le stub EHR - contrat clair, implementation temporaire
    // remplacable plus tard par un envoi reel.
    @Override
    public void notifier(Utilisateur destinataire, String sujet, String message) {
        log.info("Notification a {} ({}) - sujet: {} - message: {}",
                destinataire.getEmail(), destinataire.getRole(), sujet, message);
    }
}