package com.afriland.dottel.notifications.service;

import com.afriland.dottel.notifications.api.EvenementNotification;
import com.afriland.dottel.utilisateurs.api.DestinataireNotificationDto;
import com.afriland.dottel.utilisateurs.model.enums.RoleEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

    private static final DestinataireNotificationDto CRH =
            new DestinataireNotificationDto("s.nkolo@afrilandfirstbank.cm", RoleEnum.CRH);

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationEventListener notificationEventListener;

    @Test
    void surEvenementNotification_transmetLesTroisChampsAuCanal() {
        notificationEventListener.surEvenementNotification(new EvenementNotification(
                CRH, "Etat mensuel a valider", "L'etat mensuel 8/2026 attend votre validation."));

        verify(notificationService).notifier(CRH, "Etat mensuel a valider",
                "L'etat mensuel 8/2026 attend votre validation.");
    }

    // Decision N-2 (Sprint MM.13) : l'ecouteur s'execute APRES commit, donc les
    // donnees SONT deja en base. Laisser remonter une exception d'envoi
    // transformerait un 200 legitime en 500 affiche a l'acteur -- un serveur
    // mail injoignable ne doit jamais produire cet effet.
    @Test
    void surEvenementNotification_echecDEnvoi_nEstPasPropage() {
        doThrow(new MailSendException("Serveur SMTP injoignable"))
                .when(notificationService).notifier(any(), any(), any());

        assertThatCode(() -> notificationEventListener.surEvenementNotification(new EvenementNotification(
                CRH, "Etat mensuel a valider", "L'etat mensuel 8/2026 attend votre validation.")))
                .doesNotThrowAnyException();
    }
}
