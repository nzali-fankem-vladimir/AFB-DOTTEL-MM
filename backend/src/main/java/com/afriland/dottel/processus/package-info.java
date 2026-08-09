/**
 * Module processus (chantier MM, Sprint MM.5).
 *
 * Declenchement et pilotage du processus mensuel de dotation (RG-12),
 * workflow de validation ARH -> CRH -> DRH (RG-05, RG-08), generation et
 * signature du document unique (RG-06), et publication de l'evenement de
 * cloture consomme par le module comptable externe.
 *
 * Dependances declarees (ProcessusMensuelService orchestre le mois, d'ou le
 * nombre de dependances -- reduit a des API depuis MM.2) :
 *  - beneficiaires :: api    -- identite/dotation du beneficiaire (MM.2).
 *  - referentiel :: api      -- eligibilite et resolution de grille (MM.2).
 *  - utilisateurs :: api     -- destinataires de notification, acteur
 *    courant (AuthenticatedUserService).
 *  - utilisateurs :: enums   -- RoleEnum, separation des taches (RG-08,
 *    SeparationTachesService et ProcessusMensuelService).
 *  - utilisateurs :: entity  -- DETTE TRACEE, voir
 *    utilisateurs/model/entity/package-info.java (prerequis MM.8).
 *  - audit :: api            -- EvenementAudit publie (MM.4).
 *  - notifications :: api    -- EvenementNotification publie (MM.13), depuis
 *    l'extraction de NotificationService dans son propre module.
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {
                "beneficiaires :: api",
                "referentiel :: api",
                "utilisateurs :: api",
                "utilisateurs :: enums",
                "utilisateurs :: entity",
                "audit :: api",
                "notifications :: api"
        }
)
package com.afriland.dottel.processus;
