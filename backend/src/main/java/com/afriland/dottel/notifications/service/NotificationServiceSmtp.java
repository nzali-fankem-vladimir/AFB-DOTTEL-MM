package com.afriland.dottel.notifications.service;

import com.afriland.dottel.utilisateurs.api.DestinataireNotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envoi Outlook PROVISOIRE par SMTP (Sprint MM.13), active uniquement quand
 * dottel.notifications.enabled vaut true.
 *
 * <p>Canal retenu avec l'utilisateur : SMTP plutot que l'API Microsoft Graph,
 * qui exigerait un enregistrement d'application Azure -- un quatrieme point
 * DSI en attente alors qu'il y en a deja trois (EHR, Keycloak, schema
 * Kafka).</p>
 *
 * <p>Les parametres reels du canal (serveur, port, identifiants, adresse
 * d'expedition) ne sont pas connus : tous sont lus depuis l'environnement
 * (CLAUDE.md sections 17.6 et 18.10), jamais ecrits en dur. Cette classe est
 * destinee a etre remplacee par le service de notification interne de la
 * banque -- d'ou la frontiere NotificationService, preservee.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "dottel.notifications.enabled", havingValue = "true")
class NotificationServiceSmtp implements NotificationService {

    private final JavaMailSender javaMailSender;

    @Value("${dottel.notifications.expediteur}")
    private String expediteur;

    @Override
    public void notifier(DestinataireNotificationDto destinataire, String sujet, String message) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(expediteur);
        mail.setTo(destinataire.email());
        mail.setSubject(sujet);
        mail.setText(message);

        javaMailSender.send(mail);
        log.info("Notification envoyee a {} ({}) - sujet: {}", destinataire.email(), destinataire.role(), sujet);
    }
}
