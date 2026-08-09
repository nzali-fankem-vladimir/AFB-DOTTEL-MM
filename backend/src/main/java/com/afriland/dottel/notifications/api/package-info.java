/**
 * API publique du module notifications (chantier MM, Sprint MM.13).
 *
 * EvenementNotification : l'unique type que les modules consommateurs
 * (processus, referentiel) publient pour declencher l'envoi d'une
 * notification, en remplacement de l'appel direct a
 * NotificationService.notifier() -- miroir exact d'EvenementAudit (voir
 * audit/api/package-info.java).
 */
@org.springframework.modulith.NamedInterface("api")
package com.afriland.dottel.notifications.api;
