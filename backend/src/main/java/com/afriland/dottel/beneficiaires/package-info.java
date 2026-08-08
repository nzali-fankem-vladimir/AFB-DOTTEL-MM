/**
 * Module beneficiaires (chantier MM, Sprint MM.5).
 *
 * Enrolement et gestion du referentiel des beneficiaires de la dotation
 * telephonique mensuelle : verification EHR (RG-01/RG-02 via referentiel),
 * unicite du matricule (RG-03), import/export en masse (RG-11).
 *
 * Dependances declarees :
 *  - referentiel :: api      -- eligibilite et resolution de grille (RG-01,
 *    RG-02, RG-04), API stable depuis MM.2/MM.3.
 *  - referentiel :: exception -- GrilleTarifaireIntrouvableException levee
 *    directement par EnrolementService.confirmer() : reutilisation
 *    intentionnelle actee en MM.3 pour preserver le contrat API V3.1 (meme
 *    exception, meme 400) plutot que d'en dupliquer une locale.
 *  - utilisateurs :: api     -- AuthenticatedUserService (acteur courant).
 *  - utilisateurs :: entity  -- DETTE TRACEE, voir
 *    utilisateurs/model/entity/package-info.java (prerequis MM.8).
 *  - audit :: api            -- EvenementAudit publie (MM.4).
 *
 * NB : cette dependance vers referentiel, combinee a la dependance inverse
 * de referentiel vers beneficiaires::api (cascade de renommage, couplage
 * C3 de MM.3, plus 5 lectures synchrones via compterActifsParFonction()),
 * forme un cycle de modules detecte par ModularityTests. Decision G-2 du
 * 2026-08-03 (MM.8) : ce cycle est ASSUME DEFINITIVEMENT -- il ne s'agit
 * plus d'une dette en attente de correction. Convertir la seule ecriture
 * (renommerFonction()) en evenement ne casserait pas le cycle : Spring
 * Modulith le detecte au niveau du module entier, et il suffit d'un seul
 * des 6 appels pour qu'il persiste. Or compterActifsParFonction() ne peut
 * pas devenir un evenement asynchrone : c'est une lecture synchrone qui
 * conditionne un refus HTTP 409 immediat. Cycle piné explicitement dans
 * ModularityTests, pas cache. Voir
 * docs/chantier-ajout-metier-mm/MM.8_anomalies_et_cloture_cycle.md.
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {
                "referentiel :: api",
                "referentiel :: exception",
                "utilisateurs :: api",
                "utilisateurs :: entity",
                "audit :: api"
        }
)
package com.afriland.dottel.beneficiaires;
