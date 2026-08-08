/**
 * Module referentiel (chantier MM, Sprint MM.5).
 *
 * Referentiel des fonctions eligibles et des grilles tarifaires (RG-01,
 * RG-04, RG-10), et regle d'eligibilite (RG-02) portee par EligibiliteService
 * (decision A1 de MM.0 : rattachee ici car sa seule dependance est
 * FonctionEligibleRepository).
 *
 * Dependances declarees :
 *  - beneficiaires :: api -- FonctionEligibleService.modifier() propage un
 *    renommage de code de fonction vers les beneficiaires concernes
 *    (couplage C3, MM.3 : "si la decision B de MM.0 a retenu un module
 *    referentiel unique, NE TOUCHE PAS a la creation de GrilleTarifaire" --
 *    seule la cascade de renommage reste un appel cross-module).
 *  - utilisateurs :: api  -- AuthenticatedUserService (acteur courant pour
 *    les controleurs de grille/fonction eligible).
 *  - utilisateurs :: entity -- DETTE TRACEE (retour de
 *    AuthenticatedUserService.utilisateurCourant()), voir
 *    utilisateurs/model/entity/package-info.java (prerequis MM.8).
 *  - audit :: api         -- EvenementAudit publie (MM.4).
 *
 * NB : la dependance vers beneficiaires::api, combinee a la dependance
 * inverse de beneficiaires vers referentiel::api (eligibilite/grille), forme
 * un cycle de modules detecte par ModularityTests. Decision G-2 du
 * 2026-08-03 (MM.8) : ce cycle est ASSUME DEFINITIVEMENT, pas en attente de
 * correction -- voir beneficiaires/package-info.java pour le detail complet
 * des 6 appels et le motif.
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {
                "beneficiaires :: api",
                "utilisateurs :: api",
                "utilisateurs :: entity",
                "audit :: api"
        }
)
package com.afriland.dottel.referentiel;
