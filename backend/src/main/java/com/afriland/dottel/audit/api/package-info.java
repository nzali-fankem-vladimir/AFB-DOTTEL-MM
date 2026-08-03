/**
 * API publique du module audit (chantier MM, Sprint MM.4, complete en MM.5).
 *
 * EvenementAudit (MM.4) : l'unique type que les 8 services metier publient
 * pour declencher une entree d'audit, en remplacement de l'appel direct a
 * AuditService.enregistrer() (famille de violation (c)).
 *
 * AuditService et AuditLogResponseDto (MM.5) : deplaces ici pour casser le
 * cycle audit <-> reporting detecte par ModularityTests -- AuditLogResponseDto
 * vivait auparavant dans reporting.model.dto.reporting alors qu'il type le
 * retour de la methode publique d'audit, inversant le sens de dependance
 * attendu. reporting reste seul consommateur legitime (voir
 * reporting/package-info.java).
 */
@org.springframework.modulith.NamedInterface("api")
package com.afriland.dottel.audit.api;
