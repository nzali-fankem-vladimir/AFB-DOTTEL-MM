/**
 * Module notifications (chantier MM, Sprint MM.13).
 *
 * Notification provisoire (Outlook/SMTP) des acteurs du circuit DOTTEL, en
 * ecouteur d'evenements applicatifs (EvenementNotification) plutot qu'en
 * appel synchrone direct -- meme principe que le module audit (EvenementAudit,
 * MM.4), phase transactionnelle opposee (AFTER_COMMIT au lieu de
 * BEFORE_COMMIT : une notification est un confort, pas une obligation
 * reglementaire comme RG-09, son echec ne doit jamais faire echouer
 * l'operation metier qui l'a declenchee).
 *
 * Extrait de processus/service/ au Sprint MM.13 : a l'origine (decision C.3
 * de MM.0), NotificationService n'avait qu'un seul consommateur (processus),
 * d'ou son rattachement direct a ce module. MM.13 lui ajoute un second
 * consommateur (referentiel, notification de rejet de grille tarifaire) --
 * exactement le critere qui avait justifie l'extraction du module audit
 * (multi-consommateurs) plutot que son rattachement a un seul module
 * appelant. NotificationService devient donc un point d'integration externe
 * autonome, sur le meme modele.
 */
package com.afriland.dottel.notifications;
