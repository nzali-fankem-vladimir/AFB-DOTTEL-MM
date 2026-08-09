package com.afriland.dottel.notifications.service;

import com.afriland.dottel.notifications.api.EvenementNotification;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Ecouteur AFTER_COMMIT (decision N-1, Sprint MM.13) : miroir exact
 * d'AuditEventListener, en phase transactionnelle OPPOSEE.
 *
 * <p>L'audit s'execute BEFORE_COMMIT parce que RG-09 est une obligation
 * reglementaire : rien ne doit se committer sans trace. Une notification est
 * un confort : l'envoyer avant le commit ferait partir un mail pour un
 * changement qu'un rollback ulterieur annulerait -- l'ARH serait prevenu d'une
 * validation qui n'a jamais eu lieu.</p>
 *
 * <p>fallbackExecution=true : si un futur appelant publie hors de toute
 * transaction, l'evenement s'execute immediatement au lieu d'etre
 * silencieusement ignore (meme drapeau, meme motif que pour l'audit).</p>
 *
 * <p>try/catch (decision N-2, en filet) : apres commit, une exception non
 * rattrapee remonterait encore jusqu'a la reponse HTTP et transformerait un
 * 200 legitime -- les donnees SONT en base -- en 500 affiche a l'acteur. Un
 * serveur mail injoignable ne doit jamais produire cet effet.</p>
 */
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationEventListener.class);

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void surEvenementNotification(EvenementNotification evenement) {
        try {
            notificationService.notifier(evenement.destinataire(), evenement.sujet(), evenement.message());
        } catch (RuntimeException exception) {
            LOGGER.error("Echec d'envoi de la notification a {} (sujet : {}) : {}",
                    evenement.destinataire().email(), evenement.sujet(), exception.getMessage());
        }
    }
}
