/**
 * API publique du module audit (chantier MM, Sprint MM.4).
 *
 * Seul EvenementAudit vit ici : c'est l'unique type que les 8 services
 * metier publient pour declencher une entree d'audit, en remplacement
 * de l'appel direct a AuditService.enregistrer() (famille de violation (c)).
 * Convention identique a beneficiaires/api, referentiel/api et
 * utilisateurs/api (MM.5).
 */
@org.springframework.modulith.NamedInterface("api")
package com.afriland.dottel.audit.api;
