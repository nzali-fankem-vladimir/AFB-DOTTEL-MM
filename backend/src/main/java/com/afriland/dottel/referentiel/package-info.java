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
 *  - utilisateurs :: enums -- RoleEnum, separation des taches sur le workflow
 *    des grilles (RG-05/RG-08, SeparationTachesGrilleService, Sprint MM.12).
 *    Ajoutee par l'option W-2 : strict analogue de la dependance de meme nom
 *    declaree par le module processus pour SeparationTachesService. Aucun
 *    risque de cycle -- le module utilisateurs ne declare AUCUNE dependance
 *    sortante, rien ne peut donc revenir vers referentiel par ce chemin.
 *  - utilisateurs :: entity -- DETTE TRACEE (retour de
 *    AuthenticatedUserService.utilisateurCourant()), voir
 *    utilisateurs/model/entity/package-info.java (prerequis MM.8).
 *  - audit :: api         -- EvenementAudit publie (MM.4).
 *  - notifications :: api -- EvenementNotification publie au rejet d'une
 *    grille tarifaire, vers l'ARH createur (Sprint MM.13). C'est ce second
 *    consommateur qui a justifie l'extraction de NotificationService hors du
 *    module processus : notifier depuis referentiel vers un service vivant
 *    dans processus aurait cree un cycle referentiel -> processus. Le module
 *    notifications ne declarant aucune dependance sortante autre que
 *    utilisateurs::api (lui-meme sans dependance sortante), aucun cycle n'est
 *    possible par ce chemin.
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
                "utilisateurs :: enums",
                "utilisateurs :: entity",
                "audit :: api",
                "notifications :: api"
        }
)
package com.afriland.dottel.referentiel;
