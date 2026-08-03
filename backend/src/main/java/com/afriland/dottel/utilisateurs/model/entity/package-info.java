/**
 * Entite du module utilisateurs (chantier MM, Sprint MM.5).
 *
 * DETTE ARCHITECTURALE ASSUMEE ET TRACEE (decidee avec l'utilisateur le
 * 2026-08-03, PAS un choix d'architecture definitif) : Utilisateur est
 * importee directement par beneficiaires (EnrolementService,
 * BeneficiaireService, BeneficiaireImportService), processus
 * (SignatureServiceAutonome, SignatureService, SeparationTachesService,
 * ProcessusMensuelService, DocumentService) et security.JwtUtil, au lieu de
 * passer par un DTO expose depuis utilisateurs.api.
 *
 * Cause racine : utilisateurs.api.AuthenticatedUserService.utilisateurCourant()
 * retourne directement l'entite JPA. La fermer correctement exigerait de
 * remplacer l'entite par un DTO dans les signatures des methodes concernees
 * -- un changement de signature, explicitement hors perimetre de MM.5 (regle
 * absolue : uniquement des modificateurs de visibilite et des annotations).
 *
 * PREREQUIS REEL avant la cloture definitive du chantier (MM.8, apres
 * Keycloak en MM.7) : introduire un DTO d'identite utilisateur expose par
 * AuthenticatedUserService/UtilisateurApi, et migrer les 8 usages directs
 * listes ci-dessus. Ce n'est pas une intention vague : c'est une etape a
 * planifier explicitement, MODULARITY_STATUS.md (MM.6) le rappelle.
 */
@org.springframework.modulith.NamedInterface("entity")
package com.afriland.dottel.utilisateurs.model.entity;
